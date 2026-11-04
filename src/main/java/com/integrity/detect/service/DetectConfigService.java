package com.integrity.detect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrity.auth.security.SecurityUtils;
import com.integrity.auth.security.UserPrincipal;
import com.integrity.common.api.ResultCode;
import com.integrity.common.cache.CacheService;
import com.integrity.common.exception.BizException;
import com.integrity.detect.config.ActiveConfig;
import com.integrity.detect.config.FeatureDef;
import com.integrity.detect.config.FeatureDsl;
import com.integrity.detect.config.RiskThreshold;
import com.integrity.detect.config.ScoreMap;
import com.integrity.detect.entity.DetectConfig;
import com.integrity.detect.entity.DetectConfigLog;
import com.integrity.detect.mapper.DetectConfigLogMapper;
import com.integrity.detect.mapper.DetectConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 检测配置服务：把 detect_configs 的 JSON 解析成可用的 {@link ActiveConfig}。
 *
 * <p>这是「阈值可回看」的入口：任务落库时绑定 config_id，
 * 后续按 version 读取即可复算历史样本（/api/detect/replay）。
 *
 * <p>调整规则（对齐手册「冻结后只加不改」）：调阈值不修改原版本，
 * 而是**基于当前版本派生一个新版本**，并在 detect_config_logs 留痕。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DetectConfigService {

    private static final String CACHE_KEY_ACTIVE = "detect:config:active";

    private final DetectConfigMapper detectConfigMapper;
    private final DetectConfigLogMapper configLogMapper;
    private final ObjectMapper objectMapper;
    private final CacheService cacheService;

    /** 取当前生效配置（最新一条）。带缓存，避免每次检测都解析 JSON。 */
    public ActiveConfig getActive() {
        ActiveConfig cached = cacheService.get(CACHE_KEY_ACTIVE, ActiveConfig.class);
        if (cached != null) {
            return cached;
        }
        DetectConfig row = detectConfigMapper.selectOne(new LambdaQueryWrapper<DetectConfig>()
                .orderByDesc(DetectConfig::getConfigId)
                .last("limit 1"));
        if (row == null) {
            throw new BizException(ResultCode.CONFIG_NOT_FOUND, "尚无任何检测配置，请先执行 data.sql");
        }
        ActiveConfig active = parse(row);
        cacheService.put(CACHE_KEY_ACTIVE, active, Duration.ofMinutes(30));
        return active;
    }

    /** 按版本取配置（回放用）。 */
    public ActiveConfig getByVersion(String version) {
        DetectConfig row = detectConfigMapper.selectOne(new LambdaQueryWrapper<DetectConfig>()
                .eq(DetectConfig::getVersion, version));
        if (row == null) {
            throw new BizException(ResultCode.CONFIG_NOT_FOUND, "配置版本不存在：" + version);
        }
        return parse(row);
    }

    /** 按 config_id 取配置（任务绑定的版本）。 */
    public ActiveConfig getById(Long configId) {
        DetectConfig row = detectConfigMapper.selectById(configId);
        if (row == null) {
            throw new BizException(ResultCode.CONFIG_NOT_FOUND, "配置不存在：" + configId);
        }
        return parse(row);
    }

    /** 全部版本（按版本倒序）。 */
    public List<DetectConfig> listAll() {
        return detectConfigMapper.selectList(new LambdaQueryWrapper<DetectConfig>()
                .orderByDesc(DetectConfig::getConfigId));
    }

    /** 调整记录。 */
    public List<DetectConfigLog> logs() {
        return configLogMapper.selectList(new LambdaQueryWrapper<DetectConfigLog>()
                .orderByDesc(DetectConfigLog::getLogId).last("limit 50"));
    }

    /**
     * 调整阈值：基于当前版本派生新版本并留痕。
     *
     * @param low  低关注上限
     * @param mid  中关注下限（存疑阈值）
     * @param high 高关注下限（高危阈值）
     * @param note 调整说明
     */
    @Transactional(rollbackFor = Exception.class)
    public ActiveConfig adjust(BigDecimal low, BigDecimal mid, BigDecimal high, String note) {
        if (mid == null || high == null || mid.compareTo(high) >= 0) {
            throw new BizException("存疑阈值必须小于高危阈值");
        }
        ActiveConfig base = getActive();
        DetectConfig baseRow = detectConfigMapper.selectById(base.configId());
        String newVersion = nextVersion(base.version());

        RiskThreshold nt = new RiskThreshold();
        nt.setLow(low != null ? low : mid.subtract(new BigDecimal("0.25")));
        nt.setMid(mid);
        nt.setHigh(high);

        DetectConfig row = new DetectConfig();
        row.setVersion(newVersion);
        row.setFeatureDsl(baseRow.getFeatureDsl());
        row.setRiskThreshold(writeJson(nt));
        row.setModelVersion(base.modelVersion());
        row.setFrozen(0);
        row.setCreatedAt(LocalDateTime.now());
        detectConfigMapper.insert(row);

        DetectConfigLog log = new DetectConfigLog();
        log.setVersion(newVersion);
        log.setBaseVersion(base.version());
        log.setOldThreshold(baseRow.getRiskThreshold());
        log.setNewThreshold(row.getRiskThreshold());
        UserPrincipal operator = SecurityUtils.current();
        log.setOperatorId(operator == null ? null : operator.userId());
        log.setOperatorName(operator == null ? "系统" : operator.name());
        log.setNote(note);
        log.setCreatedAt(LocalDateTime.now());
        configLogMapper.insert(log);

        cacheService.evict(CACHE_KEY_ACTIVE);
        this.log.info("检测配置调整 {} → {}，阈值 mid={} high={}", base.version(), newVersion, mid, high);
        return parse(row);
    }

    /** 构造一个仅阈值不同的临时配置（用于影响面预览，不落库）。 */
    public ActiveConfig withThreshold(BigDecimal low, BigDecimal mid, BigDecimal high) {
        ActiveConfig base = getActive();
        RiskThreshold t = new RiskThreshold();
        t.setLow(low != null ? low : mid.subtract(new BigDecimal("0.25")));
        t.setMid(mid);
        t.setHigh(high);
        return new ActiveConfig(base.configId(), base.version(), base.modelVersion(), base.dsl(), t);
    }

    /** v1 → v2。 */
    private String nextVersion(String version) {
        if (version == null || !version.startsWith("v")) {
            return "v" + System.currentTimeMillis();
        }
        try {
            return "v" + (Integer.parseInt(version.substring(1)) + 1);
        } catch (NumberFormatException e) {
            return "v" + System.currentTimeMillis();
        }
    }

    /** 数据库行 → ActiveConfig（解析 DSL 与阈值）。 */
    public ActiveConfig parse(DetectConfig row) {
        try {
            FeatureDsl dsl = parseDsl(row.getFeatureDsl());
            RiskThreshold threshold = parseThreshold(row.getRiskThreshold());
            return new ActiveConfig(row.getConfigId(), row.getVersion(), row.getModelVersion(), dsl, threshold);
        } catch (Exception e) {
            log.error("解析检测配置失败 configId={}", row.getConfigId(), e);
            throw new BizException(ResultCode.CONFIG_NOT_FOUND, "检测配置解析失败：" + e.getMessage());
        }
    }

    private FeatureDsl parseDsl(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        FeatureDsl dsl = new FeatureDsl();
        dsl.setVersion(root.path("version").asText());
        dsl.setAggregate(root.path("aggregate").asText("weighted_sum"));
        dsl.setMinChars(root.path("min_chars").asInt(200));

        List<FeatureDef> features = new ArrayList<>();
        for (JsonNode fn : root.path("features")) {
            FeatureDef f = new FeatureDef();
            f.setCode(fn.path("code").asText());
            f.setName(fn.path("name").asText());
            f.setMethod(fn.path("method").asText());
            f.setDirection(fn.path("direction").asText());
            f.setWeight(new BigDecimal(fn.path("weight").asText("0")));
            f.setTagTextTemplate(fn.path("tag_text_template").asText(""));
            Map<String, Object> scoreMap = objectMapper.convertValue(
                    fn.path("score_map"), new TypeReference<LinkedHashMap<String, Object>>() {
                    });
            f.setScoreMap(ScoreMap.from(scoreMap));
            features.add(f);
        }
        dsl.setFeatures(features);
        return dsl;
    }

    private RiskThreshold parseThreshold(String json) throws Exception {
        return objectMapper.readValue(json, RiskThreshold.class);
    }

    private String writeJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return "{}";
        }
    }
}

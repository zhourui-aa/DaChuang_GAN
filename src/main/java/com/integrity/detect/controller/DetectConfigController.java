package com.integrity.detect.controller;

import com.integrity.common.api.Result;
import com.integrity.detect.config.ActiveConfig;
import com.integrity.detect.config.FeatureDef;
import com.integrity.detect.dto.ActiveConfigVO;
import com.integrity.detect.dto.ConfigLogVO;
import com.integrity.detect.dto.ThresholdDTO;
import com.integrity.detect.entity.DetectConfig;
import com.integrity.detect.entity.DetectConfigLog;
import com.integrity.detect.service.DetectConfigService;
import com.integrity.detect.service.DetectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 检测配置接口（「模型与阈值」页）。
 *
 * <p>阈值调整**不修改原版本**，而是派生新版本并留痕（手册「冻结后只加不改」）。
 */
@Tag(name = "08 模型与阈值")
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class DetectConfigController {

    private final DetectConfigService configService;
    private final DetectService detectService;

    @Operation(summary = "当前生效配置")
    @GetMapping("/active")
    public Result<ActiveConfigVO> active() {
        return Result.ok(toVO(configService.getActive()));
    }

    @Operation(summary = "全部配置版本")
    @GetMapping("/list")
    public Result<List<DetectConfig>> list() {
        return Result.ok(configService.listAll());
    }

    @Operation(summary = "调整记录")
    @GetMapping("/logs")
    public Result<List<ConfigLogVO>> logs() {
        List<DetectConfigLog> logs = configService.logs();
        return Result.ok(logs.stream().map(l -> {
            ConfigLogVO v = new ConfigLogVO();
            v.setLogId(l.getLogId());
            v.setVersion(l.getVersion());
            v.setBaseVersion(l.getBaseVersion());
            v.setOldThreshold(l.getOldThreshold());
            v.setNewThreshold(l.getNewThreshold());
            v.setOperatorName(l.getOperatorName());
            v.setNote(l.getNote());
            v.setCreatedAt(l.getCreatedAt());
            return v;
        }).toList());
    }

    @Operation(summary = "调整阈值（派生新版本并留痕）")
    @PostMapping("/adjust")
    public Result<ActiveConfigVO> adjust(@RequestBody ThresholdDTO dto) {
        return Result.ok(toVO(configService.adjust(dto.getLow(), dto.getMid(), dto.getHigh(), dto.getNote())));
    }

    @Operation(summary = "阈值影响面预览（不落库）")
    @PostMapping("/preview")
    public Result<Map<String, Object>> preview(@RequestBody ThresholdDTO dto) {
        return Result.ok(detectService.previewThreshold(dto.getLow(), dto.getMid(), dto.getHigh(), dto.getAssignmentId()));
    }

    private ActiveConfigVO toVO(ActiveConfig cfg) {
        ActiveConfigVO vo = new ActiveConfigVO();
        vo.setConfigId(cfg.configId());
        vo.setVersion(cfg.version());
        vo.setModelVersion(cfg.modelVersion());
        vo.setMinChars(cfg.dsl().getMinChars());
        vo.setLow(cfg.threshold().getLow());
        vo.setMid(cfg.threshold().getMid());
        vo.setHigh(cfg.threshold().getHigh());
        if (cfg.dsl().getFeatures() != null) {
            vo.setFeatures(cfg.dsl().getFeatures().stream().map(f -> {
                ActiveConfigVO.FeatureBrief b = new ActiveConfigVO.FeatureBrief();
                b.setCode(f.getCode());
                b.setName(f.getName());
                b.setDirection(f.getDirection());
                b.setWeight(f.getWeight());
                return b;
            }).toList());
        }
        return vo;
    }
}

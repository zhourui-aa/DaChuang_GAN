package com.integrity.common.web;

import com.integrity.auth.mapper.SysUserMapper;
import com.integrity.common.api.Result;
import com.integrity.detect.entity.DetectConfig;
import com.integrity.detect.mapper.DetectConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查与冒烟接口。
 *
 * <p>用于确认「服务已启动 + DB 已连通 + schema/data 已灌入」，
 * 是 P0 联调前的第一道自检（对应 make health）。
 */
@Tag(name = "00 健康检查")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthController {

    private final SysUserMapper sysUserMapper;
    private final DetectConfigMapper detectConfigMapper;

    /**
     * 探活：返回应用名、时间、用户数、v1 配置版本与 DB 状态。
     * 任何一项异常都会体现在返回体里，不会 500。
     */
    @Operation(summary = "健康检查（含 DB 连通与种子数据）")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("app", "assignment-integrity");
        data.put("time", LocalDateTime.now());
        try {
            data.put("users", sysUserMapper.selectCount(null));
            DetectConfig cfg = detectConfigMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DetectConfig>()
                            .eq(DetectConfig::getVersion, "v1"));
            data.put("detectConfig", cfg == null ? "missing" : cfg.getVersion() + " / " + cfg.getModelVersion());
            data.put("db", "ok");
        } catch (Exception e) {
            data.put("db", "error: " + e.getMessage());
        }
        return Result.ok(data);
    }
}

package com.integrity.detect.config;

/**
 * 「当前生效检测配置」的装配结果：数据库行 + 解析后的 DSL + 阈值。
 *
 * <p>一次检测只解析一次，随后在整条流水线内传递，保证同一次检测内
 * 权重/阈值口径一致（可回看的另一面）。
 */
public record ActiveConfig(
        Long configId,
        String version,
        String modelVersion,
        FeatureDsl dsl,
        RiskThreshold threshold) {
}

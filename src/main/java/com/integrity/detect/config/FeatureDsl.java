package com.integrity.detect.config;

import lombok.Data;

import java.util.List;
import java.util.Optional;

/**
 * 检测特征 DSL（detect_configs.feature_dsl 的反序列化模型）。
 *
 * <p>v1 冻结：3 条主特征（困惑度/改写痕迹/行文轨迹）+ 3 条辅证
 * （句长突发度/词汇丰富度/结构规律性），权重和 = 1。
 */
@Data
public class FeatureDsl {

    /** DSL 版本 */
    private String version;

    /** 聚合方式，当前仅 weighted_sum */
    private String aggregate;

    /** 短文本门槛：字符数低于此值不计综合分 */
    private Integer minChars;

    /** 特征列表（顺序即展示顺序） */
    private List<FeatureDef> features;

    /** 按编码取特征。 */
    public Optional<FeatureDef> byCode(String code) {
        return features == null ? Optional.empty()
                : features.stream().filter(f -> code.equals(f.getCode())).findFirst();
    }
}

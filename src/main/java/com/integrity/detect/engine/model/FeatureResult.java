package com.integrity.detect.engine.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 单条特征的检测结果（落 detect_features）。
 */
@Data
@Builder
public class FeatureResult {

    /** 特征编码 */
    private String featureCode;
    /** 特征中文名 */
    private String featureName;
    /** 原始指标值（近似值在 P0 明确标注） */
    private Double featureValue;
    /** 归一化风险分 0-1 */
    private BigDecimal featureScore;
    /** 本次生效权重 */
    private BigDecimal weight;
    /** 中性自然语言描述 */
    private String featureText;
    /** STATS / LLM */
    private String source;
}

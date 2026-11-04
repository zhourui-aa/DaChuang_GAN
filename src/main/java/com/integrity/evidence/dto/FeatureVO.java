package com.integrity.evidence.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 特征明细（报告页「特征雷达 / 明细表」）。
 */
@Data
public class FeatureVO {
    private String featureCode;
    private String featureName;
    private BigDecimal featureValue;
    private BigDecimal featureScore;
    private BigDecimal weight;
    private String featureText;
    private String source;
}

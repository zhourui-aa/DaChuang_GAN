package com.integrity.detect.config;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 风险分档阈值（detect_configs.risk_threshold）。
 *
 * <p>示例：{"low":0.35,"mid":0.6,"high":0.8}
 */
@Data
public class RiskThreshold {

    /** 低关注上限 */
    private BigDecimal low;
    /** 中关注下限 */
    private BigDecimal mid;
    /** 高关注下限 */
    private BigDecimal high;

    /** 依据综合分给出关注等级文案。 */
    public String levelOf(BigDecimal score) {
        if (score == null) {
            return "低关注";
        }
        if (high != null && score.compareTo(high) >= 0) {
            return "高关注";
        }
        if (mid != null && score.compareTo(mid) >= 0) {
            return "中关注";
        }
        return "低关注";
    }
}

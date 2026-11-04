package com.integrity.detect.config;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 单条特征的定义（DSL 的 features[] 一项）。
 *
 * <p>聚合方式为 weighted_sum：risk_score = Σ(feature_score × weight)，权重之和应为 1。
 */
@Data
public class FeatureDef {

    /** 特征编码，如 PERPLEXITY */
    private String code;

    /** 中文名，如「文本困惑度」 */
    private String name;

    /** 计算方法标识：local_llm_logprob / llm_semantic_scan / stats_* … */
    private String method;

    /** 方向：inverse（越低越可疑） / direct（越高越可疑） */
    private String direction;

    /** 权重（0-1） */
    private BigDecimal weight;

    /** 分档表 */
    private ScoreMap scoreMap;

    /** 自然语言描述模板，含 {value} / {count} 占位符 */
    private String tagTextTemplate;

    /** 是否反向（越低越可疑）。 */
    public boolean isInverse() {
        return "inverse".equalsIgnoreCase(direction);
    }
}

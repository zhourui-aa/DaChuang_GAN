package com.integrity.detect.engine.feature;

import com.integrity.detect.text.TextAnalysis;

import java.util.List;

/**
 * 单个特征的提取器：只负责算「原始指标值」与「证据区间」。
 *
 * <p>分档（score_map）、权重、文案模板由引擎统一用配置处理，
 * 提取器不感知阈值，便于 P1 换算法而不动配置。
 */
public interface FeatureExtractor {

    /** 对应 DSL 的 feature_code。 */
    String code();

    /** 提取。 */
    Extraction extract(TextAnalysis analysis);

    /** 提取结果：原始值 + 命中证据区间。 */
    record Extraction(double value, List<Span> spans) {
        public static Extraction of(double value) {
            return new Extraction(value, List.of());
        }
    }

    /** 证据区间（相对 raw_text），reason 为中性描述。 */
    record Span(int begin, int end, String reason) {
    }
}

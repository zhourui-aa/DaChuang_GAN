package com.integrity.detect.engine.feature;

import com.integrity.detect.text.TextAnalysis;
import org.springframework.stereotype.Component;

/**
 * BURSTINESS · 句长突发度。
 *
 * <p>指标 = 句长（字符数）的总体方差。人工写作句长起伏大（方差高），
 * 生成文本句长趋同（方差低）。方向 inverse：越低越可疑。
 */
@Component
public class BurstinessExtractor implements FeatureExtractor {

    public static final String CODE = "BURSTINESS";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public Extraction extract(TextAnalysis analysis) {
        var sentences = analysis.sentences();
        if (sentences.size() < 2) {
            return Extraction.of(0);
        }
        double mean = sentences.stream().mapToInt(s -> s.text().length()).average().orElse(0);
        double var = sentences.stream()
                .mapToDouble(s -> Math.pow(s.text().length() - mean, 2))
                .sum() / sentences.size();
        return Extraction.of(Math.round(var * 1000.0) / 1000.0);
    }
}

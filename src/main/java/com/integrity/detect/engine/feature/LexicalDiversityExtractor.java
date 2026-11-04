package com.integrity.detect.engine.feature;

import com.integrity.detect.text.TextAnalysis;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * LEXICAL_DIVERSITY · 词汇丰富度。
 *
 * <p>指标 = 实词（剔除停用词后的词）的类符形符比 TTR = 去重词数 / 总词数。
 * 用词单一、重复度高 → TTR 低。方向 inverse：越低越可疑。
 */
@Component
public class LexicalDiversityExtractor implements FeatureExtractor {

    public static final String CODE = "LEXICAL_DIVERSITY";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public Extraction extract(TextAnalysis analysis) {
        var words = analysis.contentWords();
        if (words.isEmpty()) {
            return Extraction.of(0);
        }
        double ttr = new HashSet<>(words).size() / (double) words.size();
        return Extraction.of(Math.round(ttr * 1000.0) / 1000.0);
    }
}

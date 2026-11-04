package com.integrity.detect.engine.feature;

import com.integrity.detect.text.TextAnalysis;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * STRUCT_PATTERN · 结构规律性。
 *
 * <p>指标 = 段落「模板化展开」的比例：段首命中常见关联词，或段首若干字在其它段落重复出现。
 * 方向 direct：越模板化越可疑。
 */
@Component
public class StructPatternExtractor implements FeatureExtractor {

    public static final String CODE = "STRUCT_PATTERN";

    /** 常见段落起始关联词（模板化信号） */
    private static final List<String> OPENERS = List.of(
            "此外", "通过", "因此", "首先", "其次", "最后", "综上", "另外", "同时",
            "然而", "总之", "随着", "作为", "与此同时", "不仅如此", "值得一提");

    private static final int OPEN_LEN = 4;

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public Extraction extract(TextAnalysis analysis) {
        List<TextAnalysis.Paragraph> paras = analysis.paragraphs();
        if (paras.isEmpty()) {
            return Extraction.of(0);
        }
        Map<String, Integer> openCount = new HashMap<>();
        for (TextAnalysis.Paragraph p : paras) {
            openCount.merge(opening(p.text()), 1, Integer::sum);
        }
        int templated = 0;
        for (TextAnalysis.Paragraph p : paras) {
            String op = opening(p.text());
            boolean byOpener = OPENERS.stream().anyMatch(p.text()::startsWith);
            boolean byRepeat = openCount.getOrDefault(op, 0) >= 2;
            if (byOpener || byRepeat) {
                templated++;
            }
        }
        double ratio = templated / (double) paras.size();
        return Extraction.of(Math.round(ratio * 1000.0) / 1000.0);
    }

    private String opening(String paragraph) {
        String s = paragraph.replaceAll("\\s", "");
        return s.length() <= OPEN_LEN ? s : s.substring(0, OPEN_LEN);
    }
}

package com.integrity.detect.engine.feature;

import com.integrity.detect.text.TextAnalysis;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PARAPHRASE_TRACE · 改写痕迹（统计近似）。
 *
 * <p>指标 = 重复 4-gram（连续 4 个有效字符）占比：1 − 去重 4-gram 数 / 总 4-gram 数。
 * 同义替换密集、句式重复的文本该值偏高。方向 direct。
 *
 * <p>命中证据：出现 ≥2 次的 4-gram，取首次出现位置作为证据区间（去词语无关符）。
 */
@Component
public class ParaphraseTraceExtractor implements FeatureExtractor {

    public static final String CODE = "PARAPHRASE_TRACE";
    private static final int N = 4;

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public Extraction extract(TextAnalysis analysis) {
        String text = analysis.text();
        // 采集「有效字符」的原文下标（跳过空白）
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // 只统计实义字符（汉字/字母/数字），跳过空白与标点
            if (Character.isLetterOrDigit(c)) {
                idx.add(i);
            }
        }
        if (idx.size() < N + N) {
            return Extraction.of(0);
        }
        Map<String, List<Integer>> gramStarts = new LinkedHashMap<>();
        int total = 0;
        for (int k = 0; k + N <= idx.size(); k++) {
            StringBuilder sb = new StringBuilder(N);
            for (int j = 0; j < N; j++) {
                sb.append(text.charAt(idx.get(k + j)));
            }
            gramStarts.computeIfAbsent(sb.toString(), x -> new ArrayList<>()).add(k);
            total++;
        }
        int distinct = gramStarts.size();
        double ratio = total == 0 ? 0 : (total - distinct) / (double) total;
        ratio = Math.round(ratio * 1000.0) / 1000.0;

        // 证据：重复次数最高的若干 4-gram（不重叠，最多 3 条）
        List<Map.Entry<String, List<Integer>>> repeated = gramStarts.entrySet().stream()
                .filter(e -> e.getValue().size() >= 2)
                .sorted(Comparator.comparingInt((Map.Entry<String, List<Integer>> e) -> e.getValue().size()).reversed())
                .toList();
        List<Span> spans = new ArrayList<>();
        List<int[]> taken = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> e : repeated) {
            if (spans.size() >= 3) {
                break;
            }
            int startIdx = e.getValue().get(0);
            int begin = idx.get(startIdx);
            int end = idx.get(startIdx + N - 1) + 1;
            boolean overlap = taken.stream().anyMatch(t -> begin < t[1] && end > t[0]);
            if (overlap) {
                continue;
            }
            taken.add(new int[]{begin, end});
            String gram = e.getKey();
            spans.add(new Span(begin, end,
                    "片段「" + gram + "」在全文重复出现 " + e.getValue().size() + " 次，属结构性重复，可作为改写痕迹的定位依据。"));
        }
        return new Extraction(ratio, spans);
    }
}

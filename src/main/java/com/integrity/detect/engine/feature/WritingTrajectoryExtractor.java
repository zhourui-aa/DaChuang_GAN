package com.integrity.detect.engine.feature;

import com.integrity.detect.text.Stopwords;
import com.integrity.detect.text.TextAnalysis;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * WRITING_TRAJECTORY · 行文轨迹（P0 词面近似；甲方案）。
 *
 * <p>手册原方法为 llm_trajectory_analysis（模型判断段落是否呈「起伏—修正—回扣」）。
 * P0 无模型，采用「相邻句实词 Jaccard 相似度的均值」作为**语义平滑度**的近似：
 * 相邻句用词重叠越稳定、越高，行文越平滑；人工写作通常句间跳跃更大。
 *
 * <p>方向 direct。P1 将替换为本地 embedding（bge-small-zh）的句间语义相似度，
 * 届时本类可由 McpDetectionEngine 取代。
 */
@Component
public class WritingTrajectoryExtractor implements FeatureExtractor {

    public static final String CODE = "WRITING_TRAJECTORY";
    private static final double SPAN_THRESHOLD = 0.30;

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public Extraction extract(TextAnalysis analysis) {
        List<TextAnalysis.Sentence> sentences = analysis.sentences();
        if (sentences.size() < 2) {
            return Extraction.of(0);
        }
        List<Set<String>> wordSets = new ArrayList<>();
        for (TextAnalysis.Sentence s : sentences) {
            wordSets.add(contentWordsIn(analysis, s));
        }

        double sum = 0;
        int pairs = 0;
        List<Span> spans = new ArrayList<>();
        for (int i = 0; i + 1 < sentences.size(); i++) {
            Set<String> a = wordSets.get(i);
            Set<String> b = wordSets.get(i + 1);
            Set<String> union = new HashSet<>(a);
            union.addAll(b);
            if (union.size() < 3) {
                continue;
            }
            Set<String> inter = new HashSet<>(a);
            inter.retainAll(b);
            double jac = inter.size() / (double) union.size();
            sum += jac;
            pairs++;
            if (jac >= SPAN_THRESHOLD && spans.size() < 3) {
                TextAnalysis.Sentence next = sentences.get(i + 1);
                spans.add(new Span(next.begin(), next.end(),
                        "该句与上一句实词重叠度约 " + Math.round(jac * 100) + "%，句间过渡较为平滑，可作为行文轨迹的定位依据。"));
            }
        }
        double mean = pairs == 0 ? 0 : sum / pairs;
        return new Extraction(Math.round(mean * 1000.0) / 1000.0, spans);
    }

    private Set<String> contentWordsIn(TextAnalysis analysis, TextAnalysis.Sentence s) {
        Set<String> set = new HashSet<>();
        for (TextAnalysis.Token t : analysis.tokens()) {
            if (t.begin() >= s.begin() && t.end() <= s.end()
                    && !Stopwords.ZH.contains(t.word())
                    && t.word().chars().anyMatch(Character::isLetterOrDigit)) {
                set.add(t.word());
            }
        }
        return set;
    }
}

package com.integrity.detect.engine;

import com.integrity.detect.config.ActiveConfig;
import com.integrity.detect.config.FeatureDef;
import com.integrity.detect.engine.feature.FeatureExtractor;
import com.integrity.detect.engine.model.DetectionOutput;
import com.integrity.detect.engine.model.EvidenceResult;
import com.integrity.detect.engine.model.FeatureResult;
import com.integrity.detect.text.TextAnalysis;
import com.integrity.detect.text.TextAnalyzer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 纯统计特征检测引擎（P0 默认实现）。
 *
 * <p>流程：文本分析 → 逐特征提取 → 按配置分档/加权 → 综合分与关注等级 → 置信度 → 护栏。
 *
 * <p>本引擎不含任何大模型语义分析，因此 {@code degraded=true}（对应手册降级模式）；
 * 但输出的 6 项特征与证据仍完整可用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatsDetectionEngine implements DetectionEngine {

    private static final List<String> FORBIDDEN_WORDS =
            List.of("作弊", "抄袭", "代写", "造假", "认定为", "AI代写");

    private final TextAnalyzer textAnalyzer;
    private final List<FeatureExtractor> extractors;

    @Override
    public String name() {
        return "stats";
    }

    @Override
    public DetectionOutput detect(String text, ActiveConfig cfg) {
        TextAnalysis analysis = textAnalyzer.analyze(text);
        Map<String, FeatureExtractor> byCode = extractors.stream()
                .collect(Collectors.toMap(FeatureExtractor::code, Function.identity(), (a, b) -> a));

        List<FeatureResult> features = new ArrayList<>();
        List<EvidenceResult> evidences = new ArrayList<>();

        for (FeatureDef def : cfg.dsl().getFeatures()) {
            FeatureExtractor ex = byCode.get(def.getCode());
            if (ex == null) {
                log.warn("未实现的特征提取器: {}", def.getCode());
                continue;
            }
            FeatureExtractor.Extraction ext = ex.extract(analysis);
            BigDecimal score = def.getScoreMap().evaluate(ext.value());
            String desc = render(def.getTagTextTemplate(), ext.value(), ext.spans().size());

            features.add(FeatureResult.builder()
                    .featureCode(def.getCode())
                    .featureName(def.getName())
                    .featureValue(ext.value())
                    .featureScore(score)
                    .weight(def.getWeight())
                    .featureText(desc)
                    .source("STATS")
                    .build());

            for (FeatureExtractor.Span span : ext.spans()) {
                int begin = Math.max(0, Math.min(span.begin(), text.length()));
                int end = Math.max(begin, Math.min(span.end(), text.length()));
                if (end <= begin) {
                    continue;
                }
                evidences.add(EvidenceResult.builder()
                        .featureCode(def.getCode())
                        .beginPos(begin)
                        .endPos(end)
                        // 由引擎统一从原文截取，保证 100% 可溯源
                        .snippet(text.substring(begin, end))
                        .reason(span.reason())
                        .build());
            }
        }

        boolean shortText = analysis.charCount() < cfg.dsl().getMinChars();

        BigDecimal riskScore;
        String riskLevel;
        if (shortText) {
            riskScore = null;
            riskLevel = "证据不足";
        } else {
            riskScore = weightedSum(features);
            riskLevel = cfg.threshold().levelOf(riskScore);
        }

        BigDecimal confidence = computeConfidence(features, evidences, shortText);

        // 护栏：置信度偏低时不得判「高关注」
        boolean guardrailAdjust = false;
        if (!shortText && confidence.compareTo(new BigDecimal("0.40")) < 0) {
            if ("高关注".equals(riskLevel)) {
                riskLevel = "中关注";
                guardrailAdjust = true;
            }
        }

        String summary = buildSummary(cfg, features, evidences, shortText, analysis.charCount(), guardrailAdjust);
        String suggestion = "建议教师结合写作过程材料（提纲、草稿、参考资料、写作时间线）与课堂表现人工复核后再行判断。";

        // 兜底护栏：出现定性词则记录（当前文案均为中性，正常不触发）
        if (containsForbidden(summary) || containsForbidden(suggestion)) {
            log.warn("护栏命中定性词，已回退为中性说明");
            summary = "系统检测已完成，输出特征证据与置信度，供教师复核参考，不作为处理依据。";
        }

        return DetectionOutput.builder()
                .features(features)
                .evidences(evidences)
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .confidence(confidence)
                .summary(summary)
                .reviewSuggestion(suggestion)
                .degraded(true)
                .modelVersion(cfg.modelVersion())
                .insufficientEvidence(shortText)
                .build();
    }

    /** 加权求和并按可用权重归一（缺失特征时仍得 0-1 分）。 */
    private BigDecimal weightedSum(List<FeatureResult> features) {
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal wsum = BigDecimal.ZERO;
        for (FeatureResult f : features) {
            if (f.getFeatureScore() == null || f.getWeight() == null) {
                continue;
            }
            sum = sum.add(f.getFeatureScore().multiply(f.getWeight()));
            wsum = wsum.add(f.getWeight());
        }
        if (wsum.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return sum.divide(wsum, 4, RoundingMode.HALF_UP);
    }

    /** 置信度：可用特征数 + 证据充分性（与分数独立）。 */
    private BigDecimal computeConfidence(List<FeatureResult> features, List<EvidenceResult> evidences, boolean shortText) {
        int available = (int) features.stream().filter(f -> f.getFeatureScore() != null).count();
        BigDecimal base;
        if (shortText) {
            base = new BigDecimal("0.20");
        } else if (available >= 5) {
            base = new BigDecimal("0.70");
        } else if (available >= 3) {
            base = new BigDecimal("0.50");
        } else {
            base = new BigDecimal("0.30");
        }
        if (evidences.size() >= 2) {
            base = base.add(new BigDecimal("0.10"));
        }
        return base.min(BigDecimal.ONE);
    }

    private String buildSummary(ActiveConfig cfg, List<FeatureResult> features, List<EvidenceResult> evidences,
                                boolean shortText, int charCount, boolean guardrailAdjust) {
        if (shortText) {
            return "文本长度 " + charCount + " 字，低于短文本门槛（" + cfg.dsl().getMinChars()
                    + " 字），仅输出特征明细、不计综合分。本结果供教师复核参考，不作为处理依据。";
        }
        String s = "本次检测基于本地统计特征模型（" + cfg.modelVersion() + "）生成，共输出 "
                + features.size() + " 项特征、" + evidences.size()
                + " 条可定位证据，未含大模型语义分析。本结果供教师复核参考，不作为处理依据。";
        if (guardrailAdjust) {
            s += "（置信度偏低，关注等级已按护栏下调）";
        }
        return s;
    }

    private boolean containsForbidden(String s) {
        return s != null && FORBIDDEN_WORDS.stream().anyMatch(s::contains);
    }

    /** 渲染描述模板：{value} 与 {count}。 */
    private String render(String template, double value, int count) {
        if (template == null) {
            return "";
        }
        String v = value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
        return template.replace("{value}", v).replace("{count}", String.valueOf(count));
    }
}

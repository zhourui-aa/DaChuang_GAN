package com.integrity.detect.engine.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 一次检测的完整产出（引擎 → 业务层）。
 */
@Data
@Builder
public class DetectionOutput {

    private List<FeatureResult> features = new ArrayList<>();
    private List<EvidenceResult> evidences = new ArrayList<>();

    private BigDecimal riskScore;
    private String riskLevel;
    private BigDecimal confidence;

    /** 检测说明（含「仅供参考」声明） */
    private String summary;
    /** 人工复核建议 */
    private String reviewSuggestion;

    /** 是否走了降级（P0 恒为 true：未含模型语义分析） */
    private boolean degraded;
    /** 引擎/模型版本 */
    private String modelVersion;
    /** 是否短文本证据不足 */
    private boolean insufficientEvidence;
}

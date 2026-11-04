package com.integrity.detect.engine.model;

import lombok.Builder;
import lombok.Data;

/**
 * 单条证据（落 detect_evidence）。
 *
 * <p>约束：snippet 必须等于 raw_text.substring(beginPos, endPos)，
 * 否则视为不可溯源、由引擎在写入前丢弃。
 */
@Data
@Builder
public class EvidenceResult {

    private String featureCode;
    private Integer beginPos;
    private Integer endPos;
    private String snippet;
    private String reason;
}

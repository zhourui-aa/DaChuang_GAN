package com.integrity.evidence.dto;

import lombok.Data;

/**
 * 单条证据（报告页高亮联动用）。
 */
@Data
public class EvidenceItemVO {
    private Long evidenceId;
    private String featureCode;
    private Integer beginPos;
    private Integer endPos;
    private String snippet;
    private String reason;
}

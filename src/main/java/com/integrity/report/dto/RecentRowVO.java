package com.integrity.report.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 概览页「近期检测记录」行。
 */
@Data
public class RecentRowVO {
    private Long submissionId;
    private String assignmentTitle;
    private String nameMask;
    private BigDecimal riskScore;
    private String riskLevel;
    private LocalDateTime createdAt;
}

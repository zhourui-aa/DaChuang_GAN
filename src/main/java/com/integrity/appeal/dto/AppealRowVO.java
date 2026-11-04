package com.integrity.appeal.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 申诉工单列表行。
 */
@Data
public class AppealRowVO {
    private Long appealId;
    private Long submissionId;
    private String assignmentTitle;
    private String nameMask;
    private String statement;
    private String status;
    private String riskLevel;
    private LocalDateTime createdAt;
}

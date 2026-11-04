package com.integrity.governance.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志视图。
 */
@Data
public class AuditLogVO {
    private Long logId;
    private Long operatorId;
    private String operatorName;
    private String operatorRole;
    private String action;
    private Long targetSubmission;
    private String ip;
    private LocalDateTime createdAt;
}

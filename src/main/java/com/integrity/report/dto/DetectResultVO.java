package com.integrity.report.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 检测结果摘要（工作台列表 / 轮询 / 报告头部）。
 */
@Data
public class DetectResultVO {
    private Long submissionId;
    private Long taskId;
    /** 任务状态 */
    private String status;
    /** 是否降级（P0 恒 true：未含模型语义分析） */
    private Integer degraded;
    private BigDecimal riskScore;
    private String riskLevel;
    private BigDecimal confidence;
    private String summary;
    private String reviewSuggestion;
    private String configVersion;
    private String modelVersion;
    private LocalDateTime createdAt;
}

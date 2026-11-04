package com.integrity.assignment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教师工作台的作业列表行（submissions + students + detect_results 的联合视图）。
 */
@Data
public class SubmissionRowVO {

    private Long submissionId;
    private Long assignmentId;
    private String assignmentTitle;
    private Long studentId;
    /** 已脱敏学号 */
    private String studentNoMask;
    /** 脱敏姓名（张*） */
    private String nameMask;
    private String fileName;
    private Integer charCount;
    /** 已提交 / 解析失败 / 待检测 */
    private String submitStatus;

    /** 以下三项来自 detect_results，未检测时为 null */
    private BigDecimal riskScore;
    private String riskLevel;
    private BigDecimal confidence;

    private LocalDateTime createdAt;
}

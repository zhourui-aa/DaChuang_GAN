package com.integrity.assignment.dto;

import lombok.Data;

/**
 * 单次提交结果。
 */
@Data
public class SubmitResultVO {

    /** 提交 ID */
    private Long submissionId;
    /** 触发的检测任务 ID（异步） */
    private Long taskId;
    /** 去噪后字符数 */
    private Integer charCount;
    /** 提交状态 */
    private String submitStatus;
}

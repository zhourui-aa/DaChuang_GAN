package com.integrity.appeal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 申诉复核。
 */
@Data
public class AppealReviewDTO {

    @NotNull(message = "缺少申诉工单 ID")
    private Long appealId;

    /** 复核结论：认定为人工撰写 / 证据不足 / 维持关注 */
    private String reviewConclusion;

    /** 复核说明（须写清依据哪条证据、如何排除） */
    private String reviewNote;

    /** 是否标记「阈值需调整」（回流配置迭代候选池） */
    private String thresholdFeedback;
}

package com.integrity.detect.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 阈值调整 / 预览入参。
 */
@Data
public class ThresholdDTO {
    /** 低关注上限（可空，默认 mid-0.25） */
    private BigDecimal low;
    /** 中关注下限（存疑阈值） */
    private BigDecimal mid;
    /** 高关注下限（高危阈值） */
    private BigDecimal high;
    /** 调整说明 */
    private String note;
    /** 预览范围：按作业过滤（可空=全部） */
    private Long assignmentId;
}

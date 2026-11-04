package com.integrity.report.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 回放结果的一行：某提交在指定配置版本下的新旧对比。
 */
@Data
public class ReplayItemVO {
    private Long submissionId;
    /** 历史结果等级 */
    private String oldLevel;
    private BigDecimal oldScore;
    /** 按指定版本复算的等级 */
    private String newLevel;
    private BigDecimal newScore;
    /** 等级是否发生变化 */
    private boolean changed;
}

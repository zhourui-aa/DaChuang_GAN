package com.integrity.report.dto;

import lombok.Data;

/**
 * 关注等级计数（看板分布用）。
 */
@Data
public class LevelCount {
    private String level;
    private Integer cnt;
}

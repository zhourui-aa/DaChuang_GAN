package com.integrity.detect.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 配置调整记录视图。
 */
@Data
public class ConfigLogVO {
    private Long logId;
    private String version;
    private String baseVersion;
    private String oldThreshold;
    private String newThreshold;
    private String operatorName;
    private String note;
    private LocalDateTime createdAt;
}

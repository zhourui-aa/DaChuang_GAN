package com.integrity.detect.dto;

import lombok.Data;

/**
 * 检测请求。
 */
@Data
public class DetectRequest {
    /** 提交 ID */
    private Long submissionId;
    /** 指定配置版本（可选，默认当前生效版本；回放时用） */
    private String configVersion;
}

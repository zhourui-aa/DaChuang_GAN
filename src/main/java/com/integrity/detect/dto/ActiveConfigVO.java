package com.integrity.detect.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 当前生效配置视图（模型与阈值页）。
 */
@Data
public class ActiveConfigVO {
    private Long configId;
    private String version;
    private String modelVersion;
    private Integer minChars;
    private BigDecimal low;
    private BigDecimal mid;
    private BigDecimal high;
    private List<FeatureBrief> features = new ArrayList<>();

    /** 特征简要 */
    @Data
    public static class FeatureBrief {
        private String code;
        private String name;
        private String direction;
        private BigDecimal weight;
    }
}

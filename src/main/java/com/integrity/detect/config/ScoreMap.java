package com.integrity.detect.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 一个特征的完整分档表（DSL 的 score_map，有序）。
 *
 * <p>把原始指标值映射为 0-1 的风险分。顺序敏感：先写先匹配。
 */
public class ScoreMap {

    private final List<ScoreBand> bands = new ArrayList<>();

    /** 从 JSON 反序列化得到的 LinkedHashMap 构建（保持书写顺序）。 */
    public static ScoreMap from(Map<String, Object> map) {
        ScoreMap sm = new ScoreMap();
        if (map != null) {
            map.forEach((k, v) -> sm.bands.add(ScoreBand.parse(k, new BigDecimal(v.toString()))));
        }
        return sm;
    }

    /**
     * 求值：返回首个命中的风险分；全部未命中时回落到 0（不误报为可疑）。
     */
    public BigDecimal evaluate(double value) {
        for (ScoreBand b : bands) {
            if (b.matches(value)) {
                return b.getScore();
            }
        }
        return BigDecimal.ZERO;
    }

    public List<ScoreBand> getBands() {
        return bands;
    }

    public boolean isEmpty() {
        return bands.isEmpty();
    }
}

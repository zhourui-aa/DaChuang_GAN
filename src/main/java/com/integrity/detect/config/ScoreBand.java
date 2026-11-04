package com.integrity.detect.config;

import java.math.BigDecimal;

/**
 * 特征分档的一条规则（来自 DSL 的 score_map 一行，如 "8-15": 0.6）。
 *
 * <p>区间语义（左闭右开，避免相邻档重叠歧义）：
 * <ul>
 *   <li>{@code <=x} → (-∞, x]</li>
 *   <li>{@code <x}  → (-∞, x)</li>
 *   <li>{@code >=x} → [x, +∞)</li>
 *   <li>{@code >x}  → (x, +∞)</li>
 *   <li>{@code a-b} → [a, b)</li>
 * </ul>
 * 求值时按 score_map 的书写顺序取「首个命中」，因此顺序即优先级。
 */
public class ScoreBand {

    /** 原始写法（用于日志/回显） */
    private final String raw;
    private final Double min;
    private final boolean minInclusive;
    private final Double max;
    private final boolean maxInclusive;
    /** 该档对应的归一化风险分 */
    private final BigDecimal score;

    private ScoreBand(String raw, Double min, boolean minInclusive,
                      Double max, boolean maxInclusive, BigDecimal score) {
        this.raw = raw;
        this.min = min;
        this.minInclusive = minInclusive;
        this.max = max;
        this.maxInclusive = maxInclusive;
        this.score = score;
    }

    /** 解析形如 "<=8" / "8-15" / ">30" 的键。 */
    public static ScoreBand parse(String key, BigDecimal score) {
        String k = key.trim();
        if (k.startsWith("<=")) {
            return new ScoreBand(k, null, true, Double.parseDouble(k.substring(2)), true, score);
        }
        if (k.startsWith(">=")) {
            return new ScoreBand(k, Double.parseDouble(k.substring(2)), true, null, true, score);
        }
        if (k.startsWith("<")) {
            return new ScoreBand(k, null, true, Double.parseDouble(k.substring(1)), false, score);
        }
        if (k.startsWith(">")) {
            return new ScoreBand(k, Double.parseDouble(k.substring(1)), false, null, true, score);
        }
        // "a-b"
        int dash = k.indexOf('-');
        double a = Double.parseDouble(k.substring(0, dash).trim());
        double b = Double.parseDouble(k.substring(dash + 1).trim());
        return new ScoreBand(k, a, true, b, false, score);
    }

    /** 判断值是否落在本档。 */
    public boolean matches(double v) {
        if (min != null && (minInclusive ? v < min : v <= min)) {
            return false;
        }
        if (max != null && (maxInclusive ? v > max : v >= max)) {
            return false;
        }
        return true;
    }

    public BigDecimal getScore() {
        return score;
    }

    @Override
    public String toString() {
        return raw + "=" + score;
    }
}

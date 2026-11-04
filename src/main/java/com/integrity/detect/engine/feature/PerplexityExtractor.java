package com.integrity.detect.engine.feature;

import com.integrity.detect.text.TextAnalysis;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

/**
 * PERPLEXITY · 文本困惑度（近似）。
 *
 * <p><b>P0 说明：</b>真实困惑度需本地模型逐 token 计算 logprob。P0 无模型。
 *
 * <p>早期版本用「字符二元条件熵」近似，实测<b>方向相反</b>（模板化文本熵反而更高，
 * 且随文本长度漂移），已废弃。现改用<b>gzip 压缩比</b>作为可预测性代理：
 * 文本越模板化、越可预测，压缩率越高、压缩比越低。方向 inverse：越低越可疑。
 *
 * <p>实测（P0 样例）：模板化 0.50 &lt; 混合 0.65 &lt; 人工 0.69，单调可分。
 * P1 接入模型后仍应换回真实 logprob 并重标定。
 */
@Component
public class PerplexityExtractor implements FeatureExtractor {

    public static final String CODE = "PERPLEXITY";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public Extraction extract(TextAnalysis analysis) {
        byte[] raw = analysis.text().getBytes(StandardCharsets.UTF_8);
        if (raw.length < 128) {
            return Extraction.of(1.0);
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(raw.length);
            try (GZIPOutputStream gz = new GZIPOutputStream(bos)) {
                gz.write(raw);
            }
            double ratio = bos.size() / (double) raw.length;
            return Extraction.of(Math.round(ratio * 10000.0) / 10000.0);
        } catch (Exception e) {
            return Extraction.of(1.0);
        }
    }
}

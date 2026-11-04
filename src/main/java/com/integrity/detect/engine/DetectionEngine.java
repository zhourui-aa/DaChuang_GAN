package com.integrity.detect.engine;

import com.integrity.detect.config.ActiveConfig;
import com.integrity.detect.engine.model.DetectionOutput;

/**
 * 检测引擎抽象。
 *
 * <p>P0 实现为 {@code StatsDetectionEngine}（纯统计特征，无 LLM）；
 * P1 新增 {@code McpDetectionEngine}（经 MCP 调 Python LangGraph 服务），
 * 由配置 {@code integrity.detect.engine} 切换，业务层无需改动。
 */
public interface DetectionEngine {

    /** 引擎标识（写日志与 model_version）。 */
    String name();

    /**
     * 对纯文本执行检测。
     *
     * @param text 已清洗的 raw_text（证据区间以此为坐标系）
     * @param cfg  当前生效配置（权重/阈值已解析）
     */
    DetectionOutput detect(String text, ActiveConfig cfg);
}

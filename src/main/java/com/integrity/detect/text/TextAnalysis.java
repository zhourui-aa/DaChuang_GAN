package com.integrity.detect.text;

import java.util.List;

/**
 * 一次文本分析的中间结果（特征提取的共享上下文，避免重复分词/分句）。
 *
 * <p>所有区间 [begin,end) 均相对 submissions.raw_text，供证据定位直接复用。
 */
public record TextAnalysis(
        String text,
        int charCount,
        List<Sentence> sentences,
        List<Paragraph> paragraphs,
        /** 分词结果（已剔除停用词与空白），供词汇丰富度等使用 */
        List<String> contentWords,
        /** 全部词（未过滤），保留区间 */
        List<Token> tokens) {

    /** 句子及其原文字符区间。 */
    public record Sentence(String text, int begin, int end) {
    }

    /** 段落及其原文字符区间。 */
    public record Paragraph(String text, int begin, int end) {
    }

    /** 词及其原文字符区间。 */
    public record Token(String word, int begin, int end) {
    }
}

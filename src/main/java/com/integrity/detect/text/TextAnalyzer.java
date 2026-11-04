package com.integrity.detect.text;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本分析器：把 raw_text 切成句子、段落、词，并标注每段在原文字符区间。
 *
 * <p>分句以中文标点（。！？；…）与换行为界；区间相对 raw_text 定位，
 * 因此下游证据可以「原样回指」。分词用 jieba（手册 §1）。
 */
@Component
public class TextAnalyzer {

    private static final String SENT_TERMINATORS = "。！？!?；;…";

    private final JiebaSegmenter segmenter = new JiebaSegmenter();

    public TextAnalysis analyze(String text) {
        String t = text == null ? "" : text;
        List<TextAnalysis.Sentence> sentences = splitSentences(t);
        List<TextAnalysis.Paragraph> paragraphs = splitParagraphs(t);

        List<SegToken> segTokens = segmenter.process(t, JiebaSegmenter.SegMode.SEARCH);
        List<TextAnalysis.Token> tokens = new ArrayList<>(segTokens.size());
        List<String> contentWords = new ArrayList<>();
        for (SegToken st : segTokens) {
            String w = st.word.strip();
            if (w.isEmpty()) {
                continue;
            }
            tokens.add(new TextAnalysis.Token(w, st.startOffset, st.endOffset));
            if (!Stopwords.ZH.contains(w) && w.chars().anyMatch(Character::isLetterOrDigit)) {
                contentWords.add(w);
            }
        }
        return new TextAnalysis(t, t.length(), sentences, paragraphs, contentWords, tokens);
    }

    private List<TextAnalysis.Sentence> splitSentences(String t) {
        List<TextAnalysis.Sentence> out = new ArrayList<>();
        int start = -1;
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (c == '\n') {
                if (start >= 0) {
                    addSentence(out, t, start, i);
                }
                start = -1;
                continue;
            }
            if (start < 0 && !Character.isWhitespace(c)) {
                start = i;
            }
            if (SENT_TERMINATORS.indexOf(c) >= 0 && start >= 0) {
                addSentence(out, t, start, i + 1);
                start = -1;
            }
        }
        if (start >= 0) {
            addSentence(out, t, start, t.length());
        }
        return out;
    }

    private void addSentence(List<TextAnalysis.Sentence> out, String t, int begin, int end) {
        String s = t.substring(begin, end).strip();
        if (!s.isEmpty()) {
            out.add(new TextAnalysis.Sentence(s, begin, end));
        }
    }

    private List<TextAnalysis.Paragraph> splitParagraphs(String t) {
        List<TextAnalysis.Paragraph> out = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < t.length(); i++) {
            if (t.charAt(i) == '\n') {
                addParagraph(out, t, start, i);
                start = i + 1;
            }
        }
        addParagraph(out, t, start, t.length());
        return out;
    }

    private void addParagraph(List<TextAnalysis.Paragraph> out, String t, int begin, int end) {
        String p = t.substring(begin, end).strip();
        if (!p.isEmpty()) {
            out.add(new TextAnalysis.Paragraph(p, begin, end));
        }
    }
}

package com.integrity.assignment.parser;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * .pdf 解析（Apache PDFBox）。仅支持含文本层的 PDF；
 * 扫描件/图片型 PDF 会解析出空文本，由上层判为「解析失败 / 无有效文本」。
 */
public class PdfTextParser implements TextParser {

    @Override
    public boolean supports(String ext) {
        return "pdf".equalsIgnoreCase(ext);
    }

    @Override
    public String parse(byte[] bytes, String fileName) throws Exception {
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(doc);
        }
    }
}

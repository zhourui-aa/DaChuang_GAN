package com.integrity.assignment.parser;

import com.integrity.common.api.ResultCode;
import com.integrity.common.exception.BizException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 解析器路由：按扩展名选择解析器。
 *
 * <p>当前支持 docx / txt / pdf（手册 §3.1）。旧版 .doc 不支持。
 */
@Component
public class TextParserFactory {

    private final List<TextParser> parsers = List.of(
            new DocxTextParser(), new TxtTextParser(), new PdfTextParser());

    /** 取扩展名（小写，不含点）。 */
    public static String extOf(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    public boolean supports(String fileName) {
        String ext = extOf(fileName);
        return parsers.stream().anyMatch(p -> p.supports(ext));
    }

    public String parse(byte[] bytes, String fileName) {
        String ext = extOf(fileName);
        return parsers.stream()
                .filter(p -> p.supports(ext))
                .findFirst()
                .orElseThrow(() -> new BizException(ResultCode.FILE_TYPE_UNSUPPORTED))
                .parseUnchecked(bytes, fileName);
    }
}

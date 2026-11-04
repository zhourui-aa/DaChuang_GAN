package com.integrity.assignment.parser;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * 纯文本解析：UTF-8 优先，失败回落 GBK（兼容旧机房门禁导出的 txt）。
 */
public class TxtTextParser implements TextParser {

    @Override
    public boolean supports(String ext) {
        return "txt".equalsIgnoreCase(ext);
    }

    @Override
    public String parse(byte[] bytes, String fileName) {
        String utf8 = tryDecode(bytes, StandardCharsets.UTF_8);
        if (utf8 != null) {
            return utf8;
        }
        String gbk = tryDecode(bytes, Charset.forName("GBK"));
        return gbk != null ? gbk : new String(bytes, StandardCharsets.UTF_8);
    }

    private String tryDecode(byte[] bytes, Charset charset) {
        try {
            CharsetDecoder decoder = charset.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            return decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            return null;
        }
    }
}

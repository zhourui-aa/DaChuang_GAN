package com.integrity.assignment.parser;

import com.integrity.common.api.ResultCode;
import com.integrity.common.exception.BizException;

/**
 * 文件 → 纯文本解析器。
 *
 * <p>只负责「把文件里的文字取出来」，不做清洗（清洗统一由 TextCleaner 负责），
 * 也不做任何检测。
 */
public interface TextParser {

    /** 是否支持该扩展名（小写，不含点）。 */
    boolean supports(String ext);

    /** 解析为纯文本；失败抛异常由上层转成 PARSE_FAILED。 */
    String parse(byte[] bytes, String fileName) throws Exception;

    /** 解析并把受检异常统一转成业务异常（供工厂调用）。 */
    default String parseUnchecked(byte[] bytes, String fileName) {
        try {
            return parse(bytes, fileName);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(ResultCode.PARSE_FAILED, "解析失败：" + e.getMessage());
        }
    }
}

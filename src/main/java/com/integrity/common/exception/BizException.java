package com.integrity.common.exception;

import com.integrity.common.api.ResultCode;
import lombok.Getter;

/**
 * 业务异常：可预期、可直接展示给用户的错误。
 *
 * <p>抛出后由 {@code GlobalExceptionHandler} 统一转成 {@code Result.fail(code,msg)}。
 * 非预期错误请勿用它，交给兜底处理（会被记为 error 级日志）。
 */
@Getter
public class BizException extends RuntimeException {

    /** 业务码，建议取 {@link ResultCode} */
    private final int code;

    /** 仅传文案，码取通用失败 */
    public BizException(String msg) {
        super(msg);
        this.code = ResultCode.FAIL.getCode();
    }

    /** 传标准返回码，文案取默认 */
    public BizException(ResultCode rc) {
        super(rc.getMsg());
        this.code = rc.getCode();
    }

    /** 传返回码 + 自定义文案 */
    public BizException(ResultCode rc, String msg) {
        super(msg);
        this.code = rc.getCode();
    }
}

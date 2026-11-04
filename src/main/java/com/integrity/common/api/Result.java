package com.integrity.common.api;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应体（全站接口返回格式）。
 *
 * <p>约定：HTTP 状态码恒为 200，业务成败由 {@link #code} 表达，
 * 便于前端统一拦截（code=200 取 data，否则弹 msg）。
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> implements Serializable {

    /** 业务码：200 成功；其余见 {@link ResultCode} */
    private int code;

    /** 提示信息（成功为 success，失败为可读原因） */
    private String msg;

    /** 业务数据 */
    private T data;

    /** 成功（无数据） */
    public static <T> Result<T> ok() {
        return build(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    /** 成功（带数据） */
    public static <T> Result<T> ok(T data) {
        return build(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    /** 成功（自定义提示 + 数据） */
    public static <T> Result<T> ok(String msg, T data) {
        return build(ResultCode.SUCCESS.getCode(), msg, data);
    }

    /** 失败（自定义提示） */
    public static <T> Result<T> fail(String msg) {
        return build(ResultCode.FAIL.getCode(), msg, null);
    }

    /** 失败（标准返回码，取默认文案） */
    public static <T> Result<T> fail(ResultCode rc) {
        return build(rc.getCode(), rc.getMsg(), null);
    }

    /** 失败（自定义码 + 文案） */
    public static <T> Result<T> fail(int code, String msg) {
        return build(code, msg, null);
    }

    private static <T> Result<T> build(int code, String msg, T data) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
}

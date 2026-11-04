package com.integrity.common.api;

import lombok.Getter;

/**
 * 业务返回码字典。
 *
 * <p>分段：200/400/401/403/404 对齐 HTTP 语义；1001+ 为业务专属。
 */
@Getter
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "success"),
    /** 通用失败 */
    FAIL(500, "操作失败"),

    /** 参数校验失败 */
    PARAM_ERROR(400, "参数错误"),
    /** 未登录 / token 失效（auth 模块收紧后使用） */
    UNAUTHORIZED(401, "未登录或登录已过期"),
    /** 越权（四级权限拦截） */
    FORBIDDEN(403, "无权访问"),
    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),

    // ---------- 业务码 1001+ ----------
    /** 上传文件为空 */
    FILE_EMPTY(1001, "上传文件为空"),
    /** 文件类型不支持（P0 仅 docx/txt） */
    FILE_TYPE_UNSUPPORTED(1002, "仅支持 .docx / .txt（P1 增加 .pdf）"),
    /** 文件超过 20MB 上限 */
    FILE_TOO_LARGE(1003, "文件超过 20MB 限制"),
    /** 文件解析失败（损坏 / 加密 / 无文本层） */
    PARSE_FAILED(1004, "文件解析失败"),
    /** 同一学生在同一作业下重复提交 */
    DUPLICATE_SUBMISSION(1005, "该学生在此作业下已有提交"),
    /** 检测任务不存在 */
    TASK_NOT_FOUND(1006, "检测任务不存在"),
    /** 检测配置版本不存在 */
    CONFIG_NOT_FOUND(1007, "检测配置不存在"),
    /** 检测引擎不可用（P1 模型不可用时可降级） */
    ENGINE_UNAVAILABLE(1008, "检测引擎不可用"),
    /** 申诉状态机非法流转 */
    APPEAL_STATE_ILLEGAL(1009, "申诉工单当前状态不允许该操作");

    /** 业务码 */
    private final int code;
    /** 默认文案 */
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}

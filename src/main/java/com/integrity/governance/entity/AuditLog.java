package com.integrity.governance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志（表 audit_logs）。
 *
 * <p>「最小必要 + 权限分级」的支撑：所有对提交 / 报告 / 申诉的访问都在此留痕，
 * 供越权排查与学期末复盘。
 */
@Data
@TableName("audit_logs")
public class AuditLog {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long logId;

    /** 操作人 */
    private Long operatorId;

    /** 操作人角色 */
    private String operatorRole;

    /** 动作：VIEW_DETECT / EXPORT_REPORT / REVIEW_APPEAL … */
    private String action;

    /** 目标提交 */
    private Long targetSubmission;

    /** 来源 IP */
    private String ip;

    /** 创建时间 */
    private LocalDateTime createdAt;
}

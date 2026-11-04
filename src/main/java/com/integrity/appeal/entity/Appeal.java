package com.integrity.appeal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 申诉工单（表 appeals）。
 *
 * <p>状态机：待受理 → 复核中 → 已认定 / 已驳回 → 已归档。
 * 高关注结果自动建草稿工单（手册 integrity-flag / HIGH_RISK）。
 */
@Data
@TableName("appeals")
public class Appeal {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long appealId;

    /** 关联提交 */
    private Long submissionId;

    /** 申诉学生 */
    private Long studentId;

    /** 触发申诉的检测任务 */
    private Long triggerTaskId;

    /** 学生陈述（如「写作过程说明」） */
    private String statement;

    /** 佐证附件对象键数组（JSON）：草稿、截图等 */
    private String attachKeys;

    /** 状态：待受理 / 复核中 / 已认定 / 已驳回 / 已归档 */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;
}

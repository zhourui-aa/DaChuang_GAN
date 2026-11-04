package com.integrity.assignment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业（表 assignments）。
 *
 * <p>一次检测的上下文：require_words 参与「短文本证据不足」判定（手册 §3.4 min_chars 的场景化）。
 */
@Data
@TableName("assignments")
public class Assignment {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long assignmentId;

    /** 所属课程 */
    private Long courseId;

    /** 作业标题 */
    private String title;

    /** 要求字数（低于阈值时只出特征明细、不计综合分） */
    private Integer requireWords;

    /** 是否允许申诉：1 允许 / 0 不允许 */
    private Integer allowAppeal;

    /** 截止时间 */
    private LocalDateTime deadline;

    /** 创建时间 */
    private LocalDateTime createdAt;
}

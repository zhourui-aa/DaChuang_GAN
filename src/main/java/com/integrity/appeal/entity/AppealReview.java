package com.integrity.appeal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 申诉复核记录（表 appeal_reviews）。
 *
 * <p>threshold_feedback 是「阈值迭代」的回流入口：复核结果若标记「阈值需调整」，
 * 进入配置迭代候选池（手册 integrity-appeal / APPEAL_REVIEWED）。
 */
@Data
@TableName("appeal_reviews")
public class AppealReview {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long reviewId;

    /** 关联工单 */
    private Long appealId;

    /** 复核人（教研室 / 学院） */
    private Long reviewerId;

    /** 复核结论：认定为人工撰写 / 证据不足 / 维持关注 */
    private String reviewConclusion;

    /** 复核说明（须写清依据哪条证据、如何排除） */
    private String reviewNote;

    /** 是否标记该案例为「阈值需调整」 */
    private String thresholdFeedback;

    /** 创建时间 */
    private LocalDateTime createdAt;
}

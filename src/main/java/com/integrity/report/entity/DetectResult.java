package com.integrity.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 检测结果（表 detect_results）—— 报告页直接消费的汇总。
 *
 * <p>risk_score = Σ(feature_score × weight)；risk_level 由配置阈值分档。
 * confidence 与分数分开计算：可用特征数 ≥5 且主特征都有证据 → 高；3-4 → 中；更低 → 低并提示证据不足。
 */
@Data
@TableName("detect_results")
public class DetectResult {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long resultId;

    /** 关联任务（一对一） */
    private Long taskId;

    /** 关联提交（便于按提交直查） */
    private Long submissionId;

    /** 综合风险分 0-1 */
    private BigDecimal riskScore;

    /** 关注等级：低关注 / 中关注 / 高关注 */
    private String riskLevel;

    /** 置信度 0-1（证据充分性，与分数独立） */
    private BigDecimal confidence;

    /** 检测说明（必须含「仅供参考，不作为处理依据」） */
    private String summary;

    /** 人工复核建议 */
    private String reviewSuggestion;

    /** 所用配置版本（回看用） */
    private String configVersion;

    /** 创建时间 */
    private LocalDateTime createdAt;
}

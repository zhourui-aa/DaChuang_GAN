package com.integrity.evidence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 检测特征明细（表 detect_features）—— 可解释性的数据基础。
 *
 * <p>「特征值」与「证据」分离存储（手册架构决策二）：本表只放数值与中性描述，
 * 全文片段放 detect_evidence，原文放 submissions.raw_text。
 */
@Data
@TableName("detect_features")
public class DetectFeature {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long featureId;

    /** 关联检测任务 */
    private Long taskId;

    /** 特征编码：PERPLEXITY / PARAPHRASE_TRACE / WRITING_TRAJECTORY / BURSTINESS / LEXICAL_DIVERSITY / STRUCT_PATTERN */
    private String featureCode;

    /** 特征中文名（前端直接用，免二次映射） */
    private String featureName;

    /** 原始指标值（如句长方差 6.8、类符形符比 0.31） */
    private BigDecimal featureValue;

    /** 归一化风险分 0-1（由 DSL 的 score_map 映射得到） */
    private BigDecimal featureScore;

    /** 本次生效权重（来自 config_version） */
    private BigDecimal weight;

    /** 中性自然语言描述（禁止定性词） */
    private String featureText;

    /** 来源：STATS（统计） / LLM（模型语义，P0 无） */
    private String source;
}

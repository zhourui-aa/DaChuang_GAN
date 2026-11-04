package com.integrity.detect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 检测配置（表 detect_configs）—— 「阈值可回看」的技术落点。
 *
 * <p>每次检测都在 detect_tasks 里绑定一个 config_id，因此历史报告可
 * 按版本复算（/api/detect/replay），阈值调整不会污染既有结论。
 *
 * <p>feature_dsl 是 6 条特征的定义（code/method/direction/weight/score_map/文案模板），
 * 权重之和 = 1，聚合方式 weighted_sum。
 */
@Data
@TableName("detect_configs")
public class DetectConfig {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long configId;

    /** 版本号，如 v1 / v2（冻结后只加不改） */
    private String version;

    /** 特征 DSL（JSON 字符串）：features[] + aggregate + min_chars */
    private String featureDsl;

    /** 风险分档阈值（JSON）：{"low":0.35,"mid":0.6,"high":0.8} */
    private String riskThreshold;

    /** 引擎/模型版本，如 stats-v0.1（P1 换成本地 7B 时记录模型名） */
    private String modelVersion;

    /** 是否冻结：冻结版本不可修改，只能新增版本 */
    private Integer frozen;

    /** 创建时间 */
    private LocalDateTime createdAt;
}

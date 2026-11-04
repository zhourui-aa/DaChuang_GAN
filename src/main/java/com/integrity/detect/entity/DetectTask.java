package com.integrity.detect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 检测任务（表 detect_tasks）—— 异步编排的状态机载体。
 *
 * <p>状态流转：待检测 → 检测中 → 已完成 / 已降级 / 失败。
 * degraded=1 表示走了「纯统计特征」降级分支（P0 因无 LLM，全部计算路径等同降级模式的统计侧）。
 */
@Data
@TableName("detect_tasks")
public class DetectTask {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long taskId;

    /** 关联提交 */
    private Long submissionId;

    /** 本次检测所用配置版本（可回看） */
    private Long configId;

    /** 状态：待检测 / 检测中 / 已完成 / 已降级 / 失败 */
    private String status;

    /** 是否降级：1 = 未含模型语义分析 */
    private Integer degraded;

    /** 耗时（毫秒） */
    private Integer costMs;

    /** 重试次数 */
    private Integer retryCount;

    /** 失败原因 */
    private String errorMsg;

    /** 创建时间（入队） */
    private LocalDateTime createdAt;

    /** 完成时间 */
    private LocalDateTime finishedAt;
}

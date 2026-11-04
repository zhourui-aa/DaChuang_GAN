package com.integrity.governance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标注数据集登记（表 datasets）。
 *
 * <p>对应研究内容「数据集构建」：人写 1000 / AI 生成 1000 / 留出集 200。
 * P0 仅建表，数据采集与评测回归在 P2。
 */
@Data
@TableName("datasets")
public class Dataset {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long datasetId;

    /** 数据集名称 */
    private String name;

    /** 样本类型：HUMAN / AI / HOLDOUT */
    private String sampleType;

    /** 样本数量 */
    private Integer sampleCount;

    /** MinIO 前缀（P0 为本地目录） */
    private String minioPrefix;

    /** 是否已人工标注 */
    private Integer labeled;

    /** 脱敏与使用授权说明 */
    private String consentNote;

    /** 创建时间 */
    private LocalDateTime createdAt;
}

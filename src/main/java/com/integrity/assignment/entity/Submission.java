package com.integrity.assignment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业提交（表 submissions）—— 整个系统的数据源头。
 *
 * <p><b>关键：证据坐标系的唯一基准是 raw_text。</b>
 * detect_evidence.begin_pos / end_pos 都相对本字段（去噪后的纯文本）定位，
 * 前端渲染也只能渲染 raw_text，否则高亮必然错位（手册 §11）。
 */
@Data
@TableName("submissions")
public class Submission {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long submissionId;

    /** 所属作业 */
    private Long assignmentId;

    /** 提交学生 */
    private Long studentId;

    /** 原文对象键：P0 = 本地路径，P1 = MinIO 对象键 */
    private String fileKey;

    /** 原始文件名 */
    private String fileName;

    /** 解析后的纯文本（去噪、去格式）——证据定位的唯一坐标系 */
    private String rawText;

    /** 去噪后字符数（用于短文本判定与统计特征） */
    private Integer charCount;

    /** 文本指纹 SHA-256：同一文本重复提交可复用检测结果（手册 detect:dup） */
    private String textSha256;

    /** 提交状态：已提交 / 解析失败 / 待检测 */
    private String submitStatus;

    /** 创建时间 */
    private LocalDateTime createdAt;
}

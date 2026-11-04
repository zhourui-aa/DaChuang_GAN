package com.integrity.evidence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 检测证据（表 detect_evidence）—— 「可溯源」的硬约束。
 *
 * <p>每条证据必须能用「原文字符区间」+「片段原文」互相验证；
 * 定位失败（越界 / 片段与区间不一致）的证据一律丢弃，不写入报告（手册 §6.5 护栏）。
 */
@Data
@TableName("detect_evidence")
public class DetectEvidence {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long evidenceId;

    /** 关联检测任务 */
    private Long taskId;

    /** 归属特征编码 */
    private String featureCode;

    /** 证据片段在 raw_text 中的起始字符位（左闭） */
    private Integer beginPos;

    /** 结束位（右开） */
    private Integer endPos;

    /** 证据片段原文（必须等于 raw_text.substring(beginPos, endPos)） */
    private String snippet;

    /** 何以成为证据（中性描述，不得含定性词） */
    private String reason;
}

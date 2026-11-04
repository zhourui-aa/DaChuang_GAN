package com.integrity.evidence.dto;

import com.integrity.report.dto.DetectResultVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 检测详情页的完整数据（手册 §9.3 信息层级的数据源）。
 */
@Data
public class EvidenceVO {

    private Long submissionId;
    private String assignmentTitle;
    private String studentNoMask;
    private String nameMask;
    private Integer charCount;

    /** 原文（证据高亮的唯一坐标系，前端必须渲染它） */
    private String rawText;

    /** 结果摘要（含关注等级、置信度、仅供参考声明） */
    private DetectResultVO result;

    private List<FeatureVO> features = new ArrayList<>();
    private List<EvidenceItemVO> evidences = new ArrayList<>();
}

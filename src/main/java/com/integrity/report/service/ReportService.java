package com.integrity.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.integrity.appeal.entity.AppealReview;
import com.integrity.appeal.mapper.AppealMapper;
import com.integrity.appeal.mapper.AppealReviewMapper;
import com.integrity.assignment.dto.SubmissionRowVO;
import com.integrity.assignment.entity.Assignment;
import com.integrity.assignment.mapper.AssignmentMapper;
import com.integrity.assignment.mapper.SubmissionMapper;
import com.integrity.detect.engine.model.DetectionOutput;
import com.integrity.report.dto.DashboardVO;
import com.integrity.report.dto.SubmissionExportRow;
import com.integrity.report.entity.DetectResult;
import com.integrity.report.mapper.DetectResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 报告服务：结果落库、结果查询、概览看板聚合、报告导出数据。
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DetectResultMapper resultMapper;
    private final SubmissionMapper submissionMapper;
    private final AssignmentMapper assignmentMapper;
    private final AppealMapper appealMapper;
    private final AppealReviewMapper appealReviewMapper;

    /** 落库检测结果。 */
    public void saveResult(Long taskId, Long submissionId, DetectionOutput out, String configVersion) {
        DetectResult r = new DetectResult();
        r.setTaskId(taskId);
        r.setSubmissionId(submissionId);
        r.setRiskScore(out.getRiskScore());
        r.setRiskLevel(out.getRiskLevel());
        r.setConfidence(out.getConfidence());
        r.setSummary(out.getSummary());
        r.setReviewSuggestion(out.getReviewSuggestion());
        r.setConfigVersion(configVersion);
        resultMapper.insert(r);
    }

    /** 按提交取结果。 */
    public DetectResult getBySubmission(Long submissionId) {
        return resultMapper.selectOne(new LambdaQueryWrapper<DetectResult>()
                .eq(DetectResult::getSubmissionId, submissionId)
                .orderByDesc(DetectResult::getResultId)
                .last("limit 1"));
    }

    /** 概览看板。 */
    public DashboardVO dashboard() {
        DashboardVO vo = new DashboardVO();
        List<com.integrity.report.dto.LevelCount> levels = resultMapper.countByLevel();
        vo.setLevelCounts(levels);
        long detected = levels.stream().mapToLong(l -> l.getCnt() == null ? 0 : l.getCnt()).sum();
        vo.setDetected(detected);
        vo.setSuspected(levels.stream()
                .filter(l -> "中关注".equals(l.getLevel()) || "高关注".equals(l.getLevel()))
                .mapToLong(l -> l.getCnt() == null ? 0 : l.getCnt()).sum());
        vo.setAppeals(appealMapper.selectCount(null));
        vo.setConfirmed(appealReviewMapper.selectCount(new LambdaQueryWrapper<AppealReview>()
                .eq(AppealReview::getReviewConclusion, "认定为人工撰写")));
        vo.setRecent(resultMapper.selectRecent(6));
        return vo;
    }

    /** 导出数据：某作业下全部提交（已脱敏）。 */
    public List<SubmissionExportRow> exportRows(Long assignmentId) {
        List<SubmissionRowVO> rows = submissionMapper.selectRowsByAssignment(assignmentId);
        List<SubmissionExportRow> out = new ArrayList<>(rows.size());
        int i = 1;
        for (SubmissionRowVO r : rows) {
            SubmissionExportRow e = new SubmissionExportRow();
            e.setIndex(i++);
            e.setStudentNo(r.getStudentNoMask());
            e.setName(r.getNameMask());
            e.setAssignmentTitle(r.getAssignmentTitle());
            e.setCharCount(r.getCharCount());
            e.setRiskScore(r.getRiskScore() == null ? "—" : Math.round(r.getRiskScore().multiply(BigDecimal.valueOf(100)).doubleValue()) + "%");
            e.setRiskLevel(r.getRiskLevel() == null ? "待检测" : r.getRiskLevel());
            e.setConfidence(r.getConfidence() == null ? "—" : Math.round(r.getConfidence().doubleValue() * 100) + "%");
            e.setCreatedAt(r.getCreatedAt() == null ? "" : r.getCreatedAt().format(DTF));
            DetectResult res = getBySubmission(r.getSubmissionId());
            e.setSummary(res == null ? "" : res.getSummary());
            out.add(e);
        }
        return out;
    }

    /** 导出文件名：不含真实姓名（手册 §9.4）。 */
    public String exportFileName(Long assignmentId) {
        Assignment a = assignmentMapper.selectById(assignmentId);
        String title = a == null ? "作业" : a.getTitle();
        return title.replaceAll("[\\\\/:*?\"<>|]", "_") + "_检测结果.xlsx";
    }
}

package com.integrity.evidence.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.integrity.assignment.entity.Assignment;
import com.integrity.assignment.entity.Student;
import com.integrity.assignment.entity.Submission;
import com.integrity.assignment.mapper.AssignmentMapper;
import com.integrity.assignment.mapper.StudentMapper;
import com.integrity.assignment.mapper.SubmissionMapper;
import com.integrity.common.util.MaskUtil;
import com.integrity.detect.entity.DetectTask;
import com.integrity.detect.engine.model.EvidenceResult;
import com.integrity.detect.engine.model.FeatureResult;
import com.integrity.detect.mapper.DetectTaskMapper;
import com.integrity.evidence.dto.EvidenceItemVO;
import com.integrity.evidence.dto.EvidenceVO;
import com.integrity.evidence.dto.FeatureVO;
import com.integrity.evidence.entity.DetectEvidence;
import com.integrity.evidence.entity.DetectFeature;
import com.integrity.evidence.mapper.DetectEvidenceMapper;
import com.integrity.evidence.mapper.DetectFeatureMapper;
import com.integrity.report.dto.DetectResultVO;
import com.integrity.report.entity.DetectResult;
import com.integrity.report.mapper.DetectResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 特征与证据服务：写入检测特征/证据，读取详情页数据。
 */
@Service
@RequiredArgsConstructor
public class EvidenceService {

    private final DetectFeatureMapper featureMapper;
    private final DetectEvidenceMapper evidenceMapper;
    private final DetectTaskMapper taskMapper;
    private final DetectResultMapper resultMapper;
    private final SubmissionMapper submissionMapper;
    private final StudentMapper studentMapper;
    private final AssignmentMapper assignmentMapper;

    /** 落库特征明细。 */
    public void saveFeatures(Long taskId, List<FeatureResult> features) {
        for (FeatureResult f : features) {
            DetectFeature e = new DetectFeature();
            e.setTaskId(taskId);
            e.setFeatureCode(f.getFeatureCode());
            e.setFeatureName(f.getFeatureName());
            e.setFeatureValue(f.getFeatureValue() == null ? null
                    : java.math.BigDecimal.valueOf(f.getFeatureValue()));
            e.setFeatureScore(f.getFeatureScore());
            e.setWeight(f.getWeight());
            e.setFeatureText(f.getFeatureText());
            e.setSource(f.getSource());
            featureMapper.insert(e);
        }
    }

    /** 落库证据片段（引擎已保证 snippet 与区间一致）。 */
    public void saveEvidences(Long taskId, List<EvidenceResult> evidences) {
        for (EvidenceResult ev : evidences) {
            DetectEvidence e = new DetectEvidence();
            e.setTaskId(taskId);
            e.setFeatureCode(ev.getFeatureCode());
            e.setBeginPos(ev.getBeginPos());
            e.setEndPos(ev.getEndPos());
            e.setSnippet(ev.getSnippet());
            e.setReason(ev.getReason());
            evidenceMapper.insert(e);
        }
    }

    /** 详情页数据：原文 + 特征 + 证据 + 结果。 */
    public EvidenceVO detail(Long submissionId) {
        EvidenceVO vo = new EvidenceVO();
        Submission s = submissionMapper.selectById(submissionId);
        if (s == null) {
            return vo;
        }
        vo.setSubmissionId(submissionId);
        vo.setRawText(s.getRawText());
        vo.setCharCount(s.getCharCount());
        Student stu = studentMapper.selectById(s.getStudentId());
        if (stu != null) {
            vo.setNameMask(stu.getNameMask() != null ? stu.getNameMask() : MaskUtil.maskName(stu.getRealName()));
            vo.setStudentNoMask(MaskUtil.maskStudentNo(stu.getStudentNo()));
        }
        Assignment a = assignmentMapper.selectById(s.getAssignmentId());
        if (a != null) {
            vo.setAssignmentTitle(a.getTitle());
        }

        DetectTask task = latestTask(submissionId);
        if (task != null) {
            vo.setFeatures(featureMapper.selectList(new LambdaQueryWrapper<DetectFeature>()
                            .eq(DetectFeature::getTaskId, task.getTaskId())
                            .orderByAsc(DetectFeature::getFeatureId))
                    .stream().map(this::toFeatureVO).toList());
            vo.setEvidences(evidenceMapper.selectList(new LambdaQueryWrapper<DetectEvidence>()
                            .eq(DetectEvidence::getTaskId, task.getTaskId())
                            .orderByAsc(DetectEvidence::getBeginPos))
                    .stream().map(this::toEvidenceVO).toList());
        }
        DetectResult r = resultMapper.selectOne(new LambdaQueryWrapper<DetectResult>()
                .eq(DetectResult::getSubmissionId, submissionId).last("limit 1"));
        if (r != null) {
            vo.setResult(toResultVO(r, task));
        }
        return vo;
    }

    /** 取某提交最新一次任务。 */
    public DetectTask latestTask(Long submissionId) {
        return taskMapper.selectOne(new LambdaQueryWrapper<DetectTask>()
                .eq(DetectTask::getSubmissionId, submissionId)
                .orderByDesc(DetectTask::getTaskId)
                .last("limit 1"));
    }

    private FeatureVO toFeatureVO(DetectFeature e) {
        FeatureVO v = new FeatureVO();
        v.setFeatureCode(e.getFeatureCode());
        v.setFeatureName(e.getFeatureName());
        v.setFeatureValue(e.getFeatureValue());
        v.setFeatureScore(e.getFeatureScore());
        v.setWeight(e.getWeight());
        v.setFeatureText(e.getFeatureText());
        v.setSource(e.getSource());
        return v;
    }

    private EvidenceItemVO toEvidenceVO(DetectEvidence e) {
        EvidenceItemVO v = new EvidenceItemVO();
        v.setEvidenceId(e.getEvidenceId());
        v.setFeatureCode(e.getFeatureCode());
        v.setBeginPos(e.getBeginPos());
        v.setEndPos(e.getEndPos());
        v.setSnippet(e.getSnippet());
        v.setReason(e.getReason());
        return v;
    }

    /** 供 ReportService 复用。 */
    public DetectResultVO toResultVO(DetectResult r, DetectTask task) {
        DetectResultVO v = new DetectResultVO();
        v.setSubmissionId(r.getSubmissionId());
        v.setTaskId(r.getTaskId());
        v.setStatus(task == null ? "已完成" : task.getStatus());
        v.setDegraded(task == null ? 1 : task.getDegraded());
        v.setRiskScore(r.getRiskScore());
        v.setRiskLevel(r.getRiskLevel());
        v.setConfidence(r.getConfidence());
        v.setSummary(r.getSummary());
        v.setReviewSuggestion(r.getReviewSuggestion());
        v.setConfigVersion(r.getConfigVersion());
        v.setModelVersion(task == null ? null : null);
        v.setCreatedAt(r.getCreatedAt());
        return v;
    }
}

package com.integrity.detect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.integrity.assignment.entity.Submission;
import com.integrity.assignment.mapper.SubmissionMapper;
import com.integrity.common.api.ResultCode;
import com.integrity.common.exception.BizException;
import com.integrity.detect.config.ActiveConfig;
import com.integrity.detect.engine.DetectionEngine;
import com.integrity.detect.engine.model.DetectionOutput;
import com.integrity.detect.entity.DetectConfig;
import com.integrity.detect.entity.DetectTask;
import com.integrity.detect.event.DetectionCompletedEvent;
import com.integrity.detect.mapper.DetectTaskMapper;
import com.integrity.evidence.service.EvidenceService;
import com.integrity.report.dto.DetectResultVO;
import com.integrity.report.dto.ReplayItemVO;
import com.integrity.report.entity.DetectResult;
import com.integrity.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 检测编排服务：任务状态机 + 调引擎 + 落特征/证据/结果。
 *
 * <p>对应手册 integrity-detect：绑定配置版本、记录耗时/降级、失败可重试。
 * P0 检测为本地同步执行（无 MQ）；提交后由 {@code SubmissionCreatedListener} 异步触发。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DetectService {

    private final SubmissionMapper submissionMapper;
    private final DetectConfigService configService;
    private final DetectTaskMapper taskMapper;
    private final DetectionEngine engine;
    private final EvidenceService evidenceService;
    private final ReportService reportService;
    private final ApplicationEventPublisher eventPublisher;

    /** 对某次提交执行检测。 */
    @Transactional(rollbackFor = Exception.class)
    public DetectResultVO run(Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BizException(ResultCode.NOT_FOUND, "提交不存在：" + submissionId);
        }
        ActiveConfig cfg = configService.getActive();

        DetectTask task = new DetectTask();
        task.setSubmissionId(submissionId);
        task.setConfigId(cfg.configId());
        task.setStatus("检测中");
        task.setDegraded(0);
        task.setRetryCount(0);
        task.setCreatedAt(LocalDateTime.now());
        taskMapper.insert(task);

        long start = System.currentTimeMillis();
        DetectionOutput out;
        try {
            out = engine.detect(submission.getRawText(), cfg);
        } catch (Exception e) {
            log.error("检测失败 submissionId={}", submissionId, e);
            task.setStatus("失败");
            task.setErrorMsg(truncate(e.getMessage()));
            task.setCostMs((int) (System.currentTimeMillis() - start));
            task.setFinishedAt(LocalDateTime.now());
            taskMapper.updateById(task);
            return buildVO(task, null);
        }

        evidenceService.saveFeatures(task.getTaskId(), out.getFeatures());
        evidenceService.saveEvidences(task.getTaskId(), out.getEvidences());
        reportService.saveResult(task.getTaskId(), submissionId, out, cfg.version());

        task.setStatus(out.isDegraded() ? "已降级" : "已完成");
        task.setDegraded(out.isDegraded() ? 1 : 0);
        task.setCostMs((int) (System.currentTimeMillis() - start));
        task.setFinishedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        // 检测完成 → 高关注自动开申诉通道（手册 integrity-flag / HIGH_RISK）
        eventPublisher.publishEvent(new DetectionCompletedEvent(
                submissionId, submission.getAssignmentId(), out.getRiskLevel()));

        DetectResultVO vo = buildVO(task, null);
        vo.setRiskScore(out.getRiskScore());
        vo.setRiskLevel(out.getRiskLevel());
        vo.setConfidence(out.getConfidence());
        vo.setSummary(out.getSummary());
        vo.setReviewSuggestion(out.getReviewSuggestion());
        vo.setConfigVersion(cfg.version());
        vo.setModelVersion(out.getModelVersion());
        log.info("检测完成 submissionId={} taskId={} 等级={} 耗时={}ms",
                submissionId, task.getTaskId(), out.getRiskLevel(), task.getCostMs());
        return vo;
    }

    /** 查询提交的检测结果摘要（工作台轮询）。 */
    public DetectResultVO getResult(Long submissionId) {
        DetectTask task = evidenceService.latestTask(submissionId);
        DetectResult result = reportService.getBySubmission(submissionId);
        DetectResultVO vo = buildVO(task, result);
        if (task != null) {
            vo.setModelVersion(null);
        }
        return vo;
    }

    /**
     * 按配置版本回放历史样本（调阈值迭代后复算）。
     *
     * <p>只读计算，不写入数据库：返回新旧等级对比，用于判断阈值调整的影响面。
     */
    public List<ReplayItemVO> replay(String configVersion, Long assignmentId) {
        ActiveConfig cfg = configService.getByVersion(configVersion);
        List<Submission> submissions = submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                .eq(assignmentId != null, Submission::getAssignmentId, assignmentId));
        List<ReplayItemVO> items = new ArrayList<>();
        for (Submission s : submissions) {
            DetectionOutput out = engine.detect(s.getRawText(), cfg);
            DetectResult old = reportService.getBySubmission(s.getSubmissionId());
            ReplayItemVO item = new ReplayItemVO();
            item.setSubmissionId(s.getSubmissionId());
            item.setNewLevel(out.getRiskLevel());
            item.setNewScore(out.getRiskScore());
            if (old != null) {
                item.setOldLevel(old.getRiskLevel());
                item.setOldScore(old.getRiskScore());
                item.setChanged(!java.util.Objects.equals(old.getRiskLevel(), out.getRiskLevel()));
            }
            items.add(item);
        }
        return items;
    }

    /**
     * 阈值影响面预览：用「仅阈值不同」的临时配置复算全部提交，不改数据库。
     *
     * <p>返回新的等级分布与「等级发生翻转」的样本列表，供教师调整前评估后果。
     */
    public Map<String, Object> previewThreshold(BigDecimal low, BigDecimal mid, BigDecimal high, Long assignmentId) {
        ActiveConfig cfg = configService.withThreshold(low, mid, high);
        List<Submission> submissions = submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                .eq(assignmentId != null, Submission::getAssignmentId, assignmentId));
        Map<String, Long> counts = new LinkedHashMap<>();
        List<Map<String, Object>> changed = new ArrayList<>();
        for (Submission s : submissions) {
            DetectionOutput out = engine.detect(s.getRawText(), cfg);
            counts.merge(out.getRiskLevel(), 1L, Long::sum);
            DetectResult old = reportService.getBySubmission(s.getSubmissionId());
            if (old != null && !Objects.equals(old.getRiskLevel(), out.getRiskLevel())) {
                Map<String, Object> c = new LinkedHashMap<>();
                c.put("submissionId", s.getSubmissionId());
                c.put("oldLevel", old.getRiskLevel());
                c.put("newLevel", out.getRiskLevel());
                c.put("newScore", out.getRiskScore());
                changed.add(c);
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", submissions.size());
        data.put("levelCounts", counts);
        data.put("changed", changed);
        return data;
    }

    private DetectResultVO buildVO(DetectTask task, DetectResult result) {
        DetectResultVO vo = new DetectResultVO();
        if (task != null) {
            vo.setTaskId(task.getTaskId());
            vo.setSubmissionId(task.getSubmissionId());
            vo.setStatus(task.getStatus());
            vo.setDegraded(task.getDegraded());
        }
        if (result != null) {
            vo.setRiskScore(result.getRiskScore());
            vo.setRiskLevel(result.getRiskLevel());
            vo.setConfidence(result.getConfidence());
            vo.setSummary(result.getSummary());
            vo.setReviewSuggestion(result.getReviewSuggestion());
            vo.setConfigVersion(result.getConfigVersion());
            vo.setCreatedAt(result.getCreatedAt());
        }
        return vo;
    }

    private String truncate(String s) {
        if (s == null) {
            return null;
        }
        return s.length() > 280 ? s.substring(0, 280) : s;
    }
}

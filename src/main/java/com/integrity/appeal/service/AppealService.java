package com.integrity.appeal.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrity.appeal.dto.AppealCreateDTO;
import com.integrity.appeal.dto.AppealDetailVO;
import com.integrity.appeal.dto.AppealReviewDTO;
import com.integrity.appeal.dto.AppealRowVO;
import com.integrity.appeal.entity.Appeal;
import com.integrity.appeal.entity.AppealReview;
import com.integrity.appeal.mapper.AppealMapper;
import com.integrity.appeal.mapper.AppealReviewMapper;
import com.integrity.assignment.entity.Assignment;
import com.integrity.assignment.entity.Student;
import com.integrity.assignment.entity.Submission;
import com.integrity.assignment.mapper.AssignmentMapper;
import com.integrity.assignment.mapper.StudentMapper;
import com.integrity.assignment.mapper.SubmissionMapper;
import com.integrity.common.api.ResultCode;
import com.integrity.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 申诉服务：工单状态机 + 复核 + 阈值反馈。
 *
 * <p>状态机：待受理 → 复核中 → 已认定 / 已驳回 → 已归档。
 * 结论映射：认定为人工撰写 / 证据不足 → 已认定；维持关注 → 已驳回。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppealService {

    private static final Map<String, String> CONCLUSION_TO_STATUS = new HashMap<>();

    static {
        CONCLUSION_TO_STATUS.put("认定为人工撰写", "已认定");
        CONCLUSION_TO_STATUS.put("证据不足", "已认定");
        CONCLUSION_TO_STATUS.put("维持关注", "已驳回");
    }

    private final AppealMapper appealMapper;
    private final AppealReviewMapper appealReviewMapper;
    private final SubmissionMapper submissionMapper;
    private final StudentMapper studentMapper;
    private final AssignmentMapper assignmentMapper;
    private final ObjectMapper objectMapper;

    /** 发起申诉。 */
    @Transactional(rollbackFor = Exception.class)
    public Long create(AppealCreateDTO dto) {
        Submission s = submissionMapper.selectById(dto.getSubmissionId());
        if (s == null) {
            throw new BizException(ResultCode.NOT_FOUND, "提交不存在");
        }
        Assignment a = assignmentMapper.selectById(s.getAssignmentId());
        if (a != null && Integer.valueOf(0).equals(a.getAllowAppeal())) {
            throw new BizException("该作业不允许申诉");
        }
        Appeal appeal = new Appeal();
        appeal.setSubmissionId(dto.getSubmissionId());
        appeal.setStudentId(s.getStudentId());
        appeal.setTriggerTaskId(dto.getTriggerTaskId());
        appeal.setStatement(dto.getStatement());
        appeal.setAttachKeys(writeJson(dto.getAttachKeys()));
        appeal.setStatus("待受理");
        appeal.setCreatedAt(LocalDateTime.now());
        appealMapper.insert(appeal);
        log.info("申诉已创建 appealId={} submissionId={}", appeal.getAppealId(), dto.getSubmissionId());
        return appeal.getAppealId();
    }

    /** 高关注自动建草稿工单（幂等）。 */
    @Transactional(rollbackFor = Exception.class)
    public void autoDraft(Long submissionId, Long assignmentId, String riskLevel) {
        if (!"高关注".equals(riskLevel)) {
            return;
        }
        Assignment a = assignmentMapper.selectById(assignmentId);
        if (a != null && Integer.valueOf(0).equals(a.getAllowAppeal())) {
            return;
        }
        Long exists = appealMapper.selectCount(new LambdaQueryWrapper<Appeal>()
                .eq(Appeal::getSubmissionId, submissionId));
        if (exists != null && exists > 0) {
            return;
        }
        Submission s = submissionMapper.selectById(submissionId);
        if (s == null) {
            return;
        }
        Appeal appeal = new Appeal();
        appeal.setSubmissionId(submissionId);
        appeal.setStudentId(s.getStudentId());
        appeal.setStatus("待受理");
        appeal.setStatement("（系统检测为高关注，已自动开启申诉通道，等待学生补充说明与佐证材料。）");
        appeal.setCreatedAt(LocalDateTime.now());
        appealMapper.insert(appeal);
        log.info("高关注自动建申诉草稿 appealId={} submissionId={}", appeal.getAppealId(), submissionId);
    }

    /** 复核。 */
    @Transactional(rollbackFor = Exception.class)
    public void review(AppealReviewDTO dto, Long reviewerId) {
        Appeal appeal = appealMapper.selectById(dto.getAppealId());
        if (appeal == null) {
            throw new BizException(ResultCode.NOT_FOUND, "申诉工单不存在");
        }
        if ("已归档".equals(appeal.getStatus())) {
            throw new BizException(ResultCode.APPEAL_STATE_ILLEGAL, "工单已归档，不可再复核");
        }
        String state = CONCLUSION_TO_STATUS.get(dto.getReviewConclusion());
        if (state == null) {
            throw new BizException("复核结论非法：" + dto.getReviewConclusion());
        }
        AppealReview review = new AppealReview();
        review.setAppealId(appeal.getAppealId());
        review.setReviewerId(reviewerId);
        review.setReviewConclusion(dto.getReviewConclusion());
        review.setReviewNote(dto.getReviewNote());
        review.setThresholdFeedback(dto.getThresholdFeedback());
        review.setCreatedAt(LocalDateTime.now());
        appealReviewMapper.insert(review);

        appeal.setStatus(state);
        appealMapper.updateById(appeal);

        if (dto.getThresholdFeedback() != null && !dto.getThresholdFeedback().isBlank()) {
            log.info("阈值反馈入候选池 appealId={} feedback={}", appeal.getAppealId(), dto.getThresholdFeedback());
        }
    }

    /** 工单列表。 */
    public IPage<AppealRowVO> page(String status, long pageNum, long pageSize) {
        return appealMapper.selectRowPage(new Page<>(pageNum, pageSize), status);
    }

    /** 工单详情（含时间线）。 */
    public AppealDetailVO detail(Long appealId) {
        Appeal appeal = appealMapper.selectById(appealId);
        if (appeal == null) {
            throw new BizException(ResultCode.NOT_FOUND, "申诉工单不存在");
        }
        AppealDetailVO vo = new AppealDetailVO();
        vo.setAppealId(appeal.getAppealId());
        vo.setSubmissionId(appeal.getSubmissionId());
        vo.setStatement(appeal.getStatement());
        vo.setStatus(appeal.getStatus());
        vo.setCreatedAt(appeal.getCreatedAt());
        vo.setAttachKeys(readJson(appeal.getAttachKeys()));

        Submission s = submissionMapper.selectById(appeal.getSubmissionId());
        if (s != null) {
            Assignment a = assignmentMapper.selectById(s.getAssignmentId());
            vo.setAssignmentTitle(a == null ? null : a.getTitle());
            Student stu = studentMapper.selectById(s.getStudentId());
            vo.setNameMask(stu == null ? null : stu.getNameMask());
        }
        List<AppealReview> reviews = appealReviewMapper.selectList(new LambdaQueryWrapper<AppealReview>()
                .eq(AppealReview::getAppealId, appealId).orderByAsc(AppealReview::getReviewId));
        vo.setReviews(reviews);
        vo.setTimeline(buildTimeline(appeal, reviews));
        return vo;
    }

    private List<AppealDetailVO.TimelineItem> buildTimeline(Appeal appeal, List<AppealReview> reviews) {
        List<AppealDetailVO.TimelineItem> tl = new ArrayList<>();
        tl.add(item("系统出具检测报告", "已生成特征证据与关注等级", "done"));
        tl.add(item("提交申诉材料", appeal.getStatement() == null ? "" : "已提交陈述与佐证", "done"));
        boolean reviewed = !reviews.isEmpty();
        tl.add(item("教师 / 督导复核",
                reviewed ? reviews.get(0).getReviewConclusion() : "进行中",
                reviewed ? "done" : "now"));
        boolean closed = "已认定".equals(appeal.getStatus()) || "已驳回".equals(appeal.getStatus())
                || "已归档".equals(appeal.getStatus());
        tl.add(item("结果归档并通知",
                closed ? appeal.getStatus() : "待处理",
                "已归档".equals(appeal.getStatus()) ? "done" : (closed ? "now" : "todo")));
        return tl;
    }

    private AppealDetailVO.TimelineItem item(String title, String detail, String state) {
        AppealDetailVO.TimelineItem t = new AppealDetailVO.TimelineItem();
        t.setTitle(title);
        t.setDetail(detail);
        t.setState(state);
        return t;
    }

    private String writeJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list == null ? List.of() : list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> readJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}

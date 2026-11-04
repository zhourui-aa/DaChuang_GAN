package com.integrity.assignment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.integrity.assignment.dto.BatchSubmitVO;
import com.integrity.assignment.dto.SubmissionRowVO;
import com.integrity.assignment.dto.SubmitResultVO;
import com.integrity.assignment.entity.Student;
import com.integrity.assignment.entity.Submission;
import com.integrity.assignment.event.SubmissionCreatedEvent;
import com.integrity.assignment.mapper.StudentMapper;
import com.integrity.assignment.mapper.SubmissionMapper;
import com.integrity.assignment.parser.TextParserFactory;
import com.integrity.assignment.util.TextCleaner;
import com.integrity.common.api.ResultCode;
import com.integrity.common.exception.BizException;
import com.integrity.common.util.MaskUtil;
import com.integrity.common.util.TextSha256;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 提交服务：解析文件 → 清洗 → 落盘 → 入库 → 发布「已提交」事件。
 *
 * <p>检测不在这里做：提交只负责把 raw_text 准备好并广播事件，
 * 由 detect 模块监听后异步检测（对应手册 assignment → RocketMQ → detect）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionMapper submissionMapper;
    private final StudentMapper studentMapper;
    private final FileStorageService fileStorageService;
    private final TextParserFactory parserFactory;
    private final ApplicationEventPublisher eventPublisher;

    /** 单文件提交。 */
    @Transactional(rollbackFor = Exception.class)
    public SubmitResultVO submit(MultipartFile file, Long assignmentId, Long studentId) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.FILE_EMPTY);
        }
        String fileName = file.getOriginalFilename();
        if (!parserFactory.supports(fileName)) {
            throw new BizException(ResultCode.FILE_TYPE_UNSUPPORTED);
        }
        Long exists = submissionMapper.selectCount(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getAssignmentId, assignmentId)
                .eq(Submission::getStudentId, studentId));
        if (exists != null && exists > 0) {
            throw new BizException(ResultCode.DUPLICATE_SUBMISSION);
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BizException(ResultCode.PARSE_FAILED, "读取上传流失败");
        }

        String parsed = parserFactory.parse(bytes, fileName);
        String rawText = TextCleaner.clean(parsed);
        if (rawText.isBlank()) {
            throw new BizException(ResultCode.PARSE_FAILED, "未从文件中提取到有效文本");
        }

        Submission s = new Submission();
        s.setAssignmentId(assignmentId);
        s.setStudentId(studentId);
        s.setFileName(fileName);
        s.setFileKey(fileStorageService.store(bytes, fileName));
        s.setRawText(rawText);
        s.setCharCount(rawText.length());
        s.setTextSha256(TextSha256.of(rawText));
        s.setSubmitStatus("已提交");
        submissionMapper.insert(s);

        // 广播「已提交」：detect 模块监听后异步检测
        eventPublisher.publishEvent(new SubmissionCreatedEvent(s.getSubmissionId()));

        SubmitResultVO vo = new SubmitResultVO();
        vo.setSubmissionId(s.getSubmissionId());
        vo.setCharCount(s.getCharCount());
        vo.setSubmitStatus(s.getSubmitStatus());
        log.info("提交成功 submissionId={} 字数={} 文件={}", s.getSubmissionId(), s.getCharCount(), fileName);
        return vo;
    }

    /**
     * 批量提交（整班一次上传）。
     *
     * <p>学生归属通过「文件名中包含学号」自动匹配；匹配不到的进 failed 明细，
     * 不阻断其余文件（对应手册「1 QPS / 教师，单请求 ≤200 文件」）。
     */
    @Transactional(rollbackFor = Exception.class)
    public BatchSubmitVO submitBatch(MultipartFile[] files, Long assignmentId) {
        BatchSubmitVO result = new BatchSubmitVO();
        if (files == null || files.length == 0) {
            return result;
        }
        Map<String, Student> byNo = studentMapper.selectList(null).stream()
                .collect(Collectors.toMap(Student::getStudentNo, Function.identity(), (a, b) -> a));

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String matchedNo = findStudentNo(fileName, byNo.keySet());
            if (matchedNo == null) {
                result.getItems().add(BatchSubmitVO.Item.fail(fileName, null, "文件名中未匹配到学号，已跳过"));
                continue;
            }
            try {
                SubmitResultVO vo = submit(file, assignmentId, byNo.get(matchedNo).getStudentId());
                result.getItems().add(BatchSubmitVO.Item.ok(fileName, matchedNo, vo.getSubmissionId()));
            } catch (BizException e) {
                result.getItems().add(BatchSubmitVO.Item.fail(fileName, matchedNo, e.getMessage()));
            }
        }
        result.setTotal(files.length);
        result.setSuccess((int) result.getItems().stream().filter(BatchSubmitVO.Item::isSuccess).count());
        result.setFailed(result.getTotal() - result.getSuccess());
        return result;
    }

    /** 工作台列表（含脱敏与风险筛选）。 */
    public IPage<SubmissionRowVO> page(Long assignmentId, String riskLevel, long pageNum, long pageSize) {
        IPage<SubmissionRowVO> page = submissionMapper.selectRowPage(
                new Page<>(pageNum, pageSize), assignmentId, riskLevel);
        page.getRecords().forEach(r -> r.setStudentNoMask(MaskUtil.maskStudentNo(r.getStudentNoMask())));
        return page;
    }

    /** 从文件名里找出包含的学号（学号一般 6 位以上数字）。 */
    private String findStudentNo(String fileName, java.util.Set<String> studentNos) {
        if (fileName == null) {
            return null;
        }
        return studentNos.stream()
                .filter(no -> no != null && !no.isBlank() && fileName.contains(no))
                .max(java.util.Comparator.comparingInt(String::length))
                .orElse(null);
    }
}

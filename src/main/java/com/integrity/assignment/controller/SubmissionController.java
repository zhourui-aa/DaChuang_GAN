package com.integrity.assignment.controller;

import com.integrity.assignment.dto.BatchSubmitVO;
import com.integrity.assignment.dto.SubmissionRowVO;
import com.integrity.assignment.dto.SubmitResultVO;
import com.integrity.assignment.service.SubmissionService;
import com.integrity.common.api.PageResult;
import com.integrity.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 提交接口（手册 §4.2 integrity-assignment）。
 */
@Tag(name = "02 作业提交")
@RestController
@RequestMapping("/api/assignment")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @Operation(summary = "提交单份作业（docx/txt/pdf）")
    @PostMapping("/submit")
    public Result<SubmitResultVO> submit(@RequestPart("file") MultipartFile file,
                                         @RequestParam Long assignmentId,
                                         @RequestParam Long studentId) {
        return Result.ok(submissionService.submit(file, assignmentId, studentId));
    }

    @Operation(summary = "批量提交（整班上传，文件名含学号自动匹配）")
    @PostMapping("/submit-batch")
    public Result<BatchSubmitVO> submitBatch(@RequestPart("files") MultipartFile[] files,
                                             @RequestParam Long assignmentId) {
        return Result.ok(submissionService.submitBatch(files, assignmentId));
    }

    @Operation(summary = "工作台作业列表（含关注等级筛选）")
    @GetMapping("/list")
    public Result<PageResult<SubmissionRowVO>> list(@RequestParam Long assignmentId,
                                                    @RequestParam(required = false) String riskLevel,
                                                    @RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(defaultValue = "10") long size) {
        return Result.ok(PageResult.of(submissionService.page(assignmentId, riskLevel, page, size)));
    }
}

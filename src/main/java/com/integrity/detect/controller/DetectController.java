package com.integrity.detect.controller;

import com.integrity.common.api.Result;
import com.integrity.detect.dto.DetectRequest;
import com.integrity.detect.service.DetectService;
import com.integrity.report.dto.DetectResultVO;
import com.integrity.report.dto.ReplayItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 检测接口（手册 §4.2 integrity-detect）。
 */
@Tag(name = "03 检测")
@RestController
@RequestMapping("/api/detect")
@RequiredArgsConstructor
public class DetectController {

    private final DetectService detectService;

    @Operation(summary = "执行检测（提交后自动触发 / 教师手动重检）")
    @PostMapping("/run")
    public Result<DetectResultVO> run(@RequestBody DetectRequest req) {
        return Result.ok(detectService.run(req.getSubmissionId()));
    }

    @Operation(summary = "查询检测结果摘要（工作台轮询）")
    @GetMapping("/result/{submissionId}")
    public Result<DetectResultVO> result(@PathVariable Long submissionId) {
        return Result.ok(detectService.getResult(submissionId));
    }

    @Operation(summary = "按配置版本回放历史样本（调阈值后复算）")
    @PostMapping("/replay")
    public Result<List<ReplayItemVO>> replay(@RequestParam String configVersion,
                                             @RequestParam(required = false) Long assignmentId) {
        return Result.ok(detectService.replay(configVersion, assignmentId));
    }
}

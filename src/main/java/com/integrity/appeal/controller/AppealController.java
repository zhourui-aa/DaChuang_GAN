package com.integrity.appeal.controller;

import com.integrity.appeal.dto.AppealCreateDTO;
import com.integrity.appeal.dto.AppealDetailVO;
import com.integrity.appeal.dto.AppealReviewDTO;
import com.integrity.appeal.dto.AppealRowVO;
import com.integrity.appeal.service.AppealService;
import com.integrity.auth.security.SecurityUtils;
import com.integrity.common.api.PageResult;
import com.integrity.common.api.Result;
import com.integrity.governance.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 申诉接口（手册 §4.2 integrity-appeal）。
 */
@Tag(name = "06 申诉")
@RestController
@RequestMapping("/api/appeal")
@RequiredArgsConstructor
public class AppealController {

    private final AppealService appealService;
    private final AuditService auditService;

    @Operation(summary = "发起申诉（高关注结果会自动建草稿）")
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody AppealCreateDTO dto) {
        return Result.ok(appealService.create(dto));
    }

    @Operation(summary = "复核（结论 + 说明 + 是否标记阈值需调整）")
    @PostMapping("/review")
    public Result<Void> review(@Valid @RequestBody AppealReviewDTO dto) {
        appealService.review(dto, SecurityUtils.currentUserId());
        auditService.log("REVIEW_APPEAL", null);
        return Result.ok();
    }

    @Operation(summary = "工单列表（按状态筛选）")
    @GetMapping("/list")
    public Result<PageResult<AppealRowVO>> list(@RequestParam(required = false) String status,
                                                @RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "10") long size) {
        return Result.ok(PageResult.of(appealService.page(status, page, size)));
    }

    @Operation(summary = "工单详情（含时间线）")
    @GetMapping("/{appealId}")
    public Result<AppealDetailVO> detail(@PathVariable Long appealId) {
        return Result.ok(appealService.detail(appealId));
    }
}

package com.integrity.evidence.controller;

import com.integrity.common.api.Result;
import com.integrity.evidence.dto.EvidenceVO;
import com.integrity.evidence.service.EvidenceService;
import com.integrity.governance.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * 特征与证据接口（手册 §4.2 integrity-evidence）。
 */
@Tag(name = "04 特征与证据")
@RestController
@RequestMapping("/api/evidence")
@RequiredArgsConstructor
public class EvidenceController {

    private final EvidenceService evidenceService;
    private final AuditService auditService;

    @Operation(summary = "检测详情：特征明细 + 证据原文区间（供前端高亮）")
    @GetMapping("/{submissionId}")
    public Result<EvidenceVO> detail(@PathVariable Long submissionId) {
        // 访问检测详情属于敏感操作，留痕
        auditService.log("VIEW_DETECT", submissionId);
        return Result.ok(evidenceService.detail(submissionId));
    }

    @Operation(summary = "相似案例检索（P0 空实现，P1 接 ES more_like_this）")
    @PostMapping("/similar")
    public Result<List<Object>> similar() {
        return Result.ok(Collections.emptyList());
    }
}

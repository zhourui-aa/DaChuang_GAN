package com.integrity.report.controller;

import com.alibaba.excel.EasyExcel;
import com.integrity.common.api.Result;
import com.integrity.governance.service.AuditService;
import com.integrity.report.dto.DashboardVO;
import com.integrity.report.dto.SubmissionExportRow;
import com.integrity.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 报告与看板接口（手册 §4.2 integrity-report）。
 */
@Tag(name = "05 报告与看板")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final AuditService auditService;

    @Operation(summary = "教师概览看板")
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.ok(reportService.dashboard());
    }

    @Operation(summary = "导出作业检测结果（Excel，已脱敏）")
    @GetMapping("/export")
    public void export(@RequestParam Long assignmentId, HttpServletResponse response) throws IOException {
        List<SubmissionExportRow> rows = reportService.exportRows(assignmentId);
        String fileName = reportService.exportFileName(assignmentId);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition",
                "attachment;filename*=utf-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        EasyExcel.write(response.getOutputStream(), SubmissionExportRow.class)
                .sheet("检测结果").doWrite(rows);
        auditService.log("EXPORT_REPORT", null);
    }
}

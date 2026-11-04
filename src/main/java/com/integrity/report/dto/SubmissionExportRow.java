package com.integrity.report.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 报告导出行（Excel）。
 *
 * <p>对齐手册 §9.4 脱敏规范：学号只保留前 6 位 + 掩码、姓名用 name_mask，不出现真实姓名。
 */
@Data
public class SubmissionExportRow {

    @ExcelProperty("序号")
    @ColumnWidth(6)
    private Integer index;

    @ExcelProperty("学号")
    @ColumnWidth(16)
    private String studentNo;

    @ExcelProperty("姓名")
    @ColumnWidth(10)
    private String name;

    @ExcelProperty("作业")
    @ColumnWidth(30)
    private String assignmentTitle;

    @ExcelProperty("字数")
    @ColumnWidth(8)
    private Integer charCount;

    @ExcelProperty("AI 疑似度")
    @ColumnWidth(12)
    private String riskScore;

    @ExcelProperty("关注等级")
    @ColumnWidth(12)
    private String riskLevel;

    @ExcelProperty("置信度")
    @ColumnWidth(10)
    private String confidence;

    @ExcelProperty("提交时间")
    @ColumnWidth(20)
    private String createdAt;

    @ExcelProperty("说明")
    @ColumnWidth(46)
    private String summary;
}

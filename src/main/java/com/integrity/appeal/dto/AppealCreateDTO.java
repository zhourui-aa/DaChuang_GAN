package com.integrity.appeal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 发起申诉。
 */
@Data
public class AppealCreateDTO {

    @NotNull(message = "缺少提交 ID")
    private Long submissionId;

    /** 触发申诉的检测任务（可选） */
    private Long triggerTaskId;

    /** 学生陈述（写作过程说明等） */
    private String statement;

    /** 佐证附件对象键（草稿 / 截图 / 参考文献） */
    private List<String> attachKeys;
}

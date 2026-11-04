package com.integrity.appeal.dto;

import com.integrity.appeal.entity.AppealReview;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 申诉详情 + 时间线（对应前端 AppealTimeline）。
 */
@Data
public class AppealDetailVO {

    private Long appealId;
    private Long submissionId;
    private String assignmentTitle;
    private String nameMask;
    private String statement;
    private List<String> attachKeys = new ArrayList<>();
    private String status;
    private LocalDateTime createdAt;

    private List<AppealReview> reviews = new ArrayList<>();
    private List<TimelineItem> timeline = new ArrayList<>();

    /** 时间线节点 */
    @Data
    public static class TimelineItem {
        private String title;
        private String detail;
        /** done / now / todo */
        private String state;
    }
}

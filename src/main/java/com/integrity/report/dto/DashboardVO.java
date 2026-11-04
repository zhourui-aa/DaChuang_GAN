package com.integrity.report.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 教师概览看板数据。
 */
@Data
public class DashboardVO {

    /** 已检测作业数 */
    private long detected;
    /** 疑似（中/高关注）数 */
    private long suspected;
    /** 复核后认定为人工撰写数 */
    private long confirmed;
    /** 申诉工单数 */
    private long appeals;

    /** 关注等级分布 */
    private List<LevelCount> levelCounts = new ArrayList<>();
    /** 近期检测记录 */
    private List<RecentRowVO> recent = new ArrayList<>();
}

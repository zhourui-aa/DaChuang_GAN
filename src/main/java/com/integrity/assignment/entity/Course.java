package com.integrity.assignment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程（表 courses）。检测的顶层归属：作业挂在课程下，报告可按课程聚合。
 */
@Data
@TableName("courses")
public class Course {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long courseId;

    /** 课程代码（业务唯一） */
    private String courseCode;

    /** 课程名称 */
    private String courseName;

    /** 开课学院 */
    private Long collegeId;

    /** 任课教师（sys_users.user_id） */
    private Long teacherId;

    /** 学期，如 2026-2027-1 */
    private String term;

    /** 是否启用 */
    private Integer enabled;

    /** 创建时间 */
    private LocalDateTime createdAt;
}

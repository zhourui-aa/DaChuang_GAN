package com.integrity.assignment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 班级（表名 classes，避让 Java 关键字 class）。
 *
 * <p>用于看板的「班级分布」聚合；学生通过 class_id 归属班级。
 */
@Data
@TableName("classes")
public class ClassInfo {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long classId;

    /** 班级名称，如「智能制造 2301 班」 */
    private String className;

    /** 所属学院 */
    private Long collegeId;

    /** 专业 */
    private String major;

    /** 年级（入学年份） */
    private Integer grade;

    /** 创建时间 */
    private LocalDateTime createdAt;
}

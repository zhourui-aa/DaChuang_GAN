package com.integrity.assignment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生（表 students）。
 *
 * <p>学生端后置，本表 P0 仅作为「提交人」「申诉人」的载体。
 * 列表页一律用 name_mask（张*）+ student_no 脱敏展示（手册 §9.4）。
 */
@Data
@TableName("students")
public class Student {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long studentId;

    /** 学号（列表页脱敏展示，如 202304******） */
    private String studentNo;

    /** 脱敏姓名，如「张*」——对外展示字段 */
    private String nameMask;

    /** 真实姓名——仅内部核对用，绝不外显 */
    private String realName;

    /** 所属班级 */
    private Long classId;

    /** 创建时间 */
    private LocalDateTime createdAt;
}

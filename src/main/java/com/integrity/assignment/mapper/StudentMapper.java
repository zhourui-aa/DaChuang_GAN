package com.integrity.assignment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.integrity.assignment.entity.Student;

/**
 * students · Student 数据访问层（MyBatis-Plus BaseMapper）。
 *
 * <p>学生信息；对外展示一律取 name_mask。
 */
public interface StudentMapper extends BaseMapper<Student> {
}

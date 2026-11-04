package com.integrity.assignment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.integrity.assignment.entity.Assignment;
import com.integrity.assignment.entity.ClassInfo;
import com.integrity.assignment.entity.Course;
import com.integrity.assignment.entity.Student;
import com.integrity.assignment.mapper.AssignmentMapper;
import com.integrity.assignment.mapper.ClassInfoMapper;
import com.integrity.assignment.mapper.CourseMapper;
import com.integrity.assignment.mapper.StudentMapper;
import com.integrity.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 基础数据接口：供工作台的下拉框选择课程 / 作业 / 班级 / 学生。
 */
@Tag(name = "01 基础数据")
@RestController
@RequestMapping("/api/assignment")
@RequiredArgsConstructor
public class AssignmentController {

    private final CourseMapper courseMapper;
    private final AssignmentMapper assignmentMapper;
    private final ClassInfoMapper classInfoMapper;
    private final StudentMapper studentMapper;

    @Operation(summary = "课程列表")
    @GetMapping("/courses")
    public Result<List<Course>> courses() {
        return Result.ok(courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getEnabled, 1).orderByAsc(Course::getCourseId)));
    }

    @Operation(summary = "作业列表（可按课程过滤）")
    @GetMapping("/assignments")
    public Result<List<Assignment>> assignments(@RequestParam(required = false) Long courseId) {
        return Result.ok(assignmentMapper.selectList(new LambdaQueryWrapper<Assignment>()
                .eq(courseId != null, Assignment::getCourseId, courseId)
                .orderByAsc(Assignment::getAssignmentId)));
    }

    @Operation(summary = "班级列表")
    @GetMapping("/classes")
    public Result<List<ClassInfo>> classes() {
        return Result.ok(classInfoMapper.selectList(new LambdaQueryWrapper<ClassInfo>()
                .orderByAsc(ClassInfo::getClassId)));
    }

    @Operation(summary = "学生列表（脱敏姓名，可按班级过滤）")
    @GetMapping("/students")
    public Result<List<Student>> students(@RequestParam(required = false) Long classId) {
        List<Student> list = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .eq(classId != null, Student::getClassId, classId)
                .orderByAsc(Student::getStudentNo));
        // 列表接口不返回真实姓名
        list.forEach(s -> s.setStudentNo(com.integrity.common.util.MaskUtil.maskStudentNo(s.getStudentNo())));
        return Result.ok(list);
    }
}

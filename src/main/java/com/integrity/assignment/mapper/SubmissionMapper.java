package com.integrity.assignment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.integrity.assignment.dto.SubmissionRowVO;
import com.integrity.assignment.entity.Submission;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * submissions · 提交数据访问层。
 *
 * <p>除继承 BaseMapper 外，提供工作台列表的联合查询（学生脱敏 + 结果等级 + 风险筛选）。
 */
public interface SubmissionMapper extends BaseMapper<Submission> {

    /** 工作台列表：按作业分页，可附风险等级筛选。 */
    @Select("""
            <script>
            SELECT s.submission_id, s.assignment_id, s.student_id, s.file_name, s.char_count,
                   s.submit_status, s.created_at,
                   st.student_no AS student_no_mask, st.name_mask AS name_mask,
                   a.title AS assignment_title,
                   r.risk_score, r.risk_level, r.confidence
            FROM submissions s
            JOIN students st ON st.student_id = s.student_id
            JOIN assignments a ON a.assignment_id = s.assignment_id
            LEFT JOIN detect_results r ON r.result_id = (
                SELECT MAX(r2.result_id) FROM detect_results r2 WHERE r2.submission_id = s.submission_id)
            WHERE s.assignment_id = #{assignmentId}
            <if test="riskLevel != null and riskLevel != ''">
                AND r.risk_level = #{riskLevel}
            </if>
            ORDER BY s.submission_id DESC
            </script>
            """)
    IPage<SubmissionRowVO> selectRowPage(IPage<SubmissionRowVO> page,
                                         @Param("assignmentId") Long assignmentId,
                                         @Param("riskLevel") String riskLevel);

    /** 导出用：某作业下全部提交（不分页）。 */
    @Select("""
            SELECT s.submission_id, s.assignment_id, s.student_id, s.file_name, s.char_count,
                   s.submit_status, s.created_at,
                   st.student_no AS student_no_mask, st.name_mask AS name_mask,
                   a.title AS assignment_title,
                   r.risk_score, r.risk_level, r.confidence
            FROM submissions s
            JOIN students st ON st.student_id = s.student_id
            JOIN assignments a ON a.assignment_id = s.assignment_id
            LEFT JOIN detect_results r ON r.result_id = (
                SELECT MAX(r2.result_id) FROM detect_results r2 WHERE r2.submission_id = s.submission_id)
            WHERE s.assignment_id = #{assignmentId}
            ORDER BY s.submission_id
            """)
    java.util.List<SubmissionRowVO> selectRowsByAssignment(@Param("assignmentId") Long assignmentId);
}

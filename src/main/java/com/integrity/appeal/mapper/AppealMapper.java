package com.integrity.appeal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.integrity.appeal.dto.AppealRowVO;
import com.integrity.appeal.entity.Appeal;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * appeals · 申诉工单数据访问层。
 */
public interface AppealMapper extends BaseMapper<Appeal> {

    /** 工单列表（含作业名 / 脱敏姓名 / 关注等级）。 */
    @Select("""
            <script>
            SELECT ap.appeal_id, ap.submission_id, ap.statement, ap.status, ap.created_at,
                   a.title AS assignment_title, st.name_mask AS name_mask, r.risk_level AS risk_level
            FROM appeals ap
            JOIN submissions s ON s.submission_id = ap.submission_id
            JOIN assignments a ON a.assignment_id = s.assignment_id
            JOIN students st ON st.student_id = ap.student_id
            LEFT JOIN detect_results r ON r.result_id = (
                SELECT MAX(r2.result_id) FROM detect_results r2 WHERE r2.submission_id = ap.submission_id)
            <if test="status != null and status != ''">
                WHERE ap.status = #{status}
            </if>
            ORDER BY ap.appeal_id DESC
            </script>
            """)
    IPage<AppealRowVO> selectRowPage(IPage<AppealRowVO> page, @Param("status") String status);
}

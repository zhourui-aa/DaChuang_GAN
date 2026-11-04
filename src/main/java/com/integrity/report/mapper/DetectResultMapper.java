package com.integrity.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.integrity.report.dto.LevelCount;
import com.integrity.report.dto.RecentRowVO;
import com.integrity.report.entity.DetectResult;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * detect_results · 结果数据访问层（含看板聚合）。
 */
public interface DetectResultMapper extends BaseMapper<DetectResult> {

    /** 按关注等级分组计数。 */
    @Select("SELECT risk_level AS level, COUNT(*) AS cnt FROM detect_results GROUP BY risk_level")
    List<LevelCount> countByLevel();

    /** 近期检测记录（含作业名与脱敏姓名）。 */
    @Select("""
            SELECT r.submission_id, a.title AS assignment_title, st.name_mask AS name_mask,
                   r.risk_score, r.risk_level, r.created_at
            FROM detect_results r
            JOIN submissions s ON s.submission_id = r.submission_id
            JOIN assignments a ON a.assignment_id = s.assignment_id
            JOIN students st ON st.student_id = s.student_id
            ORDER BY r.result_id DESC
            LIMIT #{limit}
            """)
    List<RecentRowVO> selectRecent(@Param("limit") int limit);
}

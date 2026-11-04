package com.integrity.governance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.integrity.governance.dto.AuditLogVO;
import com.integrity.governance.entity.AuditLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * audit_logs · 审计数据访问层（含操作人姓名联查）。
 *
 * <p>支持按操作人过滤：任课教师只能看本人记录（最小必要），学院/管理员看全部。
 */
public interface AuditLogMapper extends BaseMapper<AuditLog> {

    /** 审计分页查询。operatorId 非空时只返回该操作人的记录。 */
    @Select("""
            <script>
            SELECT l.log_id, l.operator_id, u.name AS operator_name, l.operator_role,
                   l.action, l.target_submission, l.ip, l.created_at
            FROM audit_logs l
            LEFT JOIN sys_users u ON u.user_id = l.operator_id
            <where>
              <if test="operatorId != null"> AND l.operator_id = #{operatorId} </if>
              <if test="action != null and action != ''"> AND l.action = #{action} </if>
              <if test="keyword != null and keyword != ''">
                AND (l.operator_role LIKE CONCAT('%',#{keyword},'%') OR l.action LIKE CONCAT('%',#{keyword},'%'))
              </if>
            </where>
            ORDER BY l.log_id DESC
            </script>
            """)
    IPage<AuditLogVO> selectPage(IPage<AuditLogVO> page,
                                 @Param("operatorId") Long operatorId,
                                 @Param("action") String action,
                                 @Param("keyword") String keyword);
}

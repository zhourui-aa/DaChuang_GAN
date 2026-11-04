package com.integrity.governance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.integrity.governance.dto.AuditLogVO;
import com.integrity.governance.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 审计查询服务。
 */
@Service
@RequiredArgsConstructor
public class AuditQueryService {

    private final AuditLogMapper auditLogMapper;

    /**
     * @param operatorId 非空时只查该操作人（任课教师看本人）
     */
    public IPage<AuditLogVO> page(Long operatorId, String action, String keyword, long pageNum, long pageSize) {
        return auditLogMapper.selectPage(new Page<>(pageNum, pageSize), operatorId, action, keyword);
    }
}

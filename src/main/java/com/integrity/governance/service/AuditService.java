package com.integrity.governance.service;

import com.integrity.auth.security.UserPrincipal;
import com.integrity.auth.security.SecurityUtils;
import com.integrity.governance.entity.AuditLog;
import com.integrity.governance.mapper.AuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 审计日志服务（手册「最小必要 + 权限分级」的留痕支撑）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogMapper auditLogMapper;

    /** 记录一次访问/操作。 */
    public void log(String action, Long targetSubmission) {
        try {
            UserPrincipal p = SecurityUtils.current();
            AuditLog log = new AuditLog();
            log.setOperatorId(p == null || p.userId() == null ? 0L : p.userId());
            log.setOperatorRole(p == null ? "ANONYMOUS" : p.role());
            log.setAction(action);
            log.setTargetSubmission(targetSubmission);
            log.setIp(currentIp());
            log.setCreatedAt(LocalDateTime.now());
            auditLogMapper.insert(log);
        } catch (Exception e) {
            // 审计失败不应影响业务主流程
            log.warn("写审计日志失败: {}", e.getMessage());
        }
    }

    private String currentIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest req = attrs.getRequest();
            String xff = req.getHeader("X-Forwarded-For");
            return xff != null && !xff.isBlank() ? xff.split(",")[0].trim() : req.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}

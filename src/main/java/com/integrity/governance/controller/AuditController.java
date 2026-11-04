package com.integrity.governance.controller;

import com.integrity.auth.security.SecurityUtils;
import com.integrity.auth.security.UserPrincipal;
import com.integrity.common.api.PageResult;
import com.integrity.common.api.Result;
import com.integrity.governance.dto.AuditLogVO;
import com.integrity.governance.service.AuditQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审计日志查询。
 *
 * <p>最小必要：任课教师只能看到**本人**的操作记录；
 * 教研室 / 学院 / 管理员可查看全部。具体是「只读自己的」还是「看全部」
 * 在接口层按角色收敛，避免前端传参越权。
 */
@Tag(name = "09 审计日志")
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditQueryService auditQueryService;

    @Operation(summary = "审计日志分页查询（教师仅本人）")
    @GetMapping("/list")
    public Result<PageResult<AuditLogVO>> list(@RequestParam(required = false) String action,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size) {
        UserPrincipal me = SecurityUtils.current();
        // 任课教师只看自己的；其余角色看全部
        Long scopeOperatorId = (me != null && "TEACHER".equals(me.role())) ? me.userId() : null;
        return Result.ok(PageResult.of(
                auditQueryService.page(scopeOperatorId, action, keyword, page, size)));
    }
}

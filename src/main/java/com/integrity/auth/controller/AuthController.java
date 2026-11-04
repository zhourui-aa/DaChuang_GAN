package com.integrity.auth.controller;

import com.integrity.auth.dto.LoginRequest;
import com.integrity.auth.dto.LoginVO;
import com.integrity.auth.security.SecurityUtils;
import com.integrity.auth.security.UserPrincipal;
import com.integrity.auth.service.AuthService;
import com.integrity.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口。
 */
@Tag(name = "07 认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录（返回 JWT）")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req));
    }

    @Operation(summary = "当前登录人")
    @GetMapping("/me")
    public Result<UserPrincipal> me() {
        return Result.ok(SecurityUtils.current());
    }
}

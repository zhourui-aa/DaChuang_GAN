package com.integrity.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.integrity.auth.dto.LoginRequest;
import com.integrity.auth.dto.LoginVO;
import com.integrity.auth.entity.SysUser;
import com.integrity.auth.mapper.SysUserMapper;
import com.integrity.auth.security.UserPrincipal;
import com.integrity.auth.util.JwtUtil;
import com.integrity.common.api.ResultCode;
import com.integrity.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 登录认证服务。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /** 校验账号密码并签发 JWT。 */
    public LoginVO login(LoginRequest req) {
        SysUser u = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername()));
        if (u == null || !passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            throw new BizException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (Integer.valueOf(0).equals(u.getEnabled())) {
            throw new BizException(ResultCode.FORBIDDEN, "账号已停用");
        }
        UserPrincipal principal = new UserPrincipal(u.getUserId(), u.getUsername(), u.getName(),
                u.getRole(), u.getCollegeId());
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.generate(principal));
        vo.setUserId(u.getUserId());
        vo.setUsername(u.getUsername());
        vo.setName(u.getName());
        vo.setRole(u.getRole());
        vo.setExpireMinutes(jwtUtil.expireMinutes());
        return vo;
    }
}

package com.integrity.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrity.auth.filter.JwtAuthFilter;
import com.integrity.common.api.Result;
import com.integrity.common.api.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * 安全配置：无状态 JWT + 四级权限基础。
 *
 * <p>放行：登录、健康检查、接口文档；其余一律需认证。
 * 认证失败返回 401 JSON，越权返回 403 JSON（统一 {@code Result} 结构）。
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /** 无需认证的路径 */
    private static final String[] WHITELIST = {
            "/api/auth/login", "/api/health",
            "/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
            "/favicon.ico", "/error"
    };

    private final JwtAuthFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITELIST).permitAll()
                        // ---- 四级权限分级（手册「任课教师 / 教研室 / 学院 / 管理员」）----
                        .requestMatchers("/api/config/adjust", "/api/config/preview")
                        .hasAnyRole("CHAIR", "ADMIN")            // 阈值调整：教研室 / 管理员
                        // 审计查询：所有登录角色可访问，教师仅能看到本人记录（在接口层收敛）
                        .requestMatchers("/api/appeal/review")
                        .hasAnyRole("CHAIR", "COLLEGE", "ADMIN")  // 申诉认定：教研室 / 学院 / 管理员
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(this::unauthorized)
                        .accessDeniedHandler(this::forbidden))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());
        return http.build();
    }

    private void unauthorized(HttpServletRequest req, HttpServletResponse res,
                             org.springframework.security.core.AuthenticationException e) throws IOException {
        write(res, ResultCode.UNAUTHORIZED);
    }

    private void forbidden(HttpServletRequest req, HttpServletResponse res,
                           org.springframework.security.access.AccessDeniedException e) throws IOException {
        write(res, ResultCode.FORBIDDEN);
    }

    private void write(HttpServletResponse res, ResultCode rc) throws IOException {
        res.setStatus(rc.getCode());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(objectMapper.writeValueAsString(Result.fail(rc)));
    }
}

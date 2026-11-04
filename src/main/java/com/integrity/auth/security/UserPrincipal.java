package com.integrity.auth.security;

/**
 * 登录主体（写入 JWT，注入 SecurityContext）。
 *
 * @param userId    用户 ID
 * @param username  登录名
 * @param name      姓名
 * @param role      TEACHER / CHAIR / COLLEGE / ADMIN
 * @param collegeId 所属学院
 */
public record UserPrincipal(Long userId, String username, String name, String role, Long collegeId) {
}

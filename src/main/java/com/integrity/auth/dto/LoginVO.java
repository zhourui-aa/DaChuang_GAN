package com.integrity.auth.dto;

import lombok.Data;

/**
 * 登录返回。
 */
@Data
public class LoginVO {
    private String token;
    private Long userId;
    private String username;
    private String name;
    private String role;
    private long expireMinutes;
}

package com.integrity.auth.util;

import com.integrity.auth.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与解析（HS256）。
 */
@Component
public class JwtUtil {

    @Value("${integrity.jwt.secret}")
    private String secret;

    @Value("${integrity.jwt.expire-minutes:480}")
    private long expireMinutes;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 签发。 */
    public String generate(UserPrincipal p) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expireMinutes * 60_000L);
        return Jwts.builder()
                .subject(String.valueOf(p.userId()))
                .claim("username", p.username())
                .claim("name", p.name())
                .claim("role", p.role())
                .claim("collegeId", p.collegeId())
                .issuedAt(now)
                .expiration(exp)
                .signWith(key())
                .compact();
    }

    /** 解析；非法或过期返回 null。 */
    public UserPrincipal parse(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(key()).build()
                    .parseSignedClaims(token).getPayload();
            Object cid = c.get("collegeId");
            Long collegeId = cid instanceof Number n ? n.longValue() : null;
            return new UserPrincipal(
                    Long.valueOf(c.getSubject()),
                    c.get("username", String.class),
                    c.get("name", String.class),
                    c.get("role", String.class),
                    collegeId);
        } catch (Exception e) {
            return null;
        }
    }

    public long expireMinutes() {
        return expireMinutes;
    }
}

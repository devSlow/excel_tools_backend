package com.slow.excel_tools_backend.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 */
public class JwtUtil {

    private static final String SECRET = "excel_tools_backend_jwt_secret_key_2025";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRE_MS = 7 * 24 * 60 * 60 * 1000; // 7天

    /**
     * 生成 token
     *
     * @param userId 用户ID
     * @param openid 微信openid
     * @return JWT token
     */
    public static String generate(Long userId, String openid) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("openid", openid)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_MS))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 token，返回 Claims；解析失败返回 null
     */
    public static Claims parse(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 token 中获取用户ID
     */
    public static Long getUserId(String token) {
        Claims claims = parse(token);
        if (claims == null) return null;
        return Long.parseLong(claims.getSubject());
    }
}

package com.riskplatform.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * 共享 JWT 校验器（R17.1/R17.2）。
 *
 * <p>各后端服务以「资源服务器」方式校验 rule-config-service 颁发的 HS256 令牌：
 * 校验签名与过期，解析出 username（subject）与 roles。仅做校验，不签发
 * （签发由 rule-config-service 的 {@code /api/v1/auth/login} 负责）。
 *
 * <p>所有服务必须使用与签发方一致的密钥（配置项 {@code security.jwt.secret}），
 * 否则令牌校验失败 → 401。
 */
public class JwtTokenVerifier {

    private final SecretKey key;

    public JwtTokenVerifier(String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解析并校验令牌；签名错误/过期/格式非法时抛异常。
     *
     * @param token JWT 字符串（不含 "Bearer " 前缀）
     * @return 载荷 Claims（含 subject 与 roles）
     */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}

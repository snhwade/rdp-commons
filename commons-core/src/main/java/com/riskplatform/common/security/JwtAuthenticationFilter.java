package com.riskplatform.common.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 共享 JWT 鉴权过滤器（R17.1/R17.2）。
 *
 * <p>解析 {@code Authorization: Bearer <token>}，校验签名与过期后，将 roles 转为
 * {@code ROLE_*} 权限注入 {@link SecurityContextHolder}。无 token 或 token 无效时
 * 不注入认证（由各服务 SecurityConfig 决定放行还是按未认证处理 → 401）。
 *
 * <p>与 rule-config-service 的颁发逻辑一致：载荷中的 {@code roles}（如 ADMIN/OPERATOR/
 * AUDITOR）映射为 Spring Security 角色 {@code ROLE_ADMIN/ROLE_OPERATOR/ROLE_AUDITOR}。
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenVerifier verifier;

    public JwtAuthenticationFilter(JwtTokenVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = verifier.parse(token);
                List<String> roles = claims.get("roles", List.class);
                List<SimpleGrantedAuthority> authorities = (roles == null ? List.<String>of() : roles).stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .toList();
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                // token 无效/过期：清空上下文，后续按未认证处理（401）
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}

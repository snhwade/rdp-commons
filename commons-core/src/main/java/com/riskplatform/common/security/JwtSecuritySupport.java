package com.riskplatform.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 共享 JWT 安全支撑装配（R17.1/R17.2）。
 *
 * <p>提供各资源服务通用的鉴权组件：JWT 校验器、JWT 鉴权过滤器、结构化 401/403 处理器。
 * 各服务在自己的 {@code SecurityConfig} 中 {@code @Import(JwtSecuritySupport.class)} 即可复用，
 * 仅需声明各自的 {@code SecurityFilterChain}（端点放行/角色授权矩阵）。
 *
 * <p>密钥与 token 过期由配置项统一管理，须与签发方 rule-config-service 一致：
 * <ul>
 *   <li>{@code security.jwt.secret}：HS256 密钥（默认值仅用于本地开发）。</li>
 * </ul>
 */
@Configuration
public class JwtSecuritySupport {

    @Bean
    public JwtTokenVerifier jwtTokenVerifier(
            @Value("${security.jwt.secret:rdp-local-dev-secret-key-must-be-long-enough-256bit!!}") String secret) {
        return new JwtTokenVerifier(secret);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenVerifier verifier) {
        return new JwtAuthenticationFilter(verifier);
    }

    @Bean
    public RestAuthEntryPoint restAuthEntryPoint() {
        return new RestAuthEntryPoint();
    }

    @Bean
    public RestAccessDeniedHandler restAccessDeniedHandler() {
        return new RestAccessDeniedHandler();
    }
}

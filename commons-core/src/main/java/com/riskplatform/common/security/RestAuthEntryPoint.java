package com.riskplatform.common.security;

import com.riskplatform.common.error.CommonErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 未认证入口点（R17.2）。
 *
 * <p>未携带有效 JWT 访问受保护端点时，返回 401 与结构化错误体
 * {@code { "code": "SYSTEM.UNAUTHORIZED", "message": "未授权" }}，与平台统一错误体
 * {@link com.riskplatform.common.error.ErrorResponse} 保持一致。
 */
public class RestAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        writeUnauthorized(response, HttpServletResponse.SC_UNAUTHORIZED,
                CommonErrorCode.UNAUTHORIZED.code(), "未通过身份校验，拒绝访问");
    }

    static void writeUnauthorized(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String body = "{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}";
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    }
}

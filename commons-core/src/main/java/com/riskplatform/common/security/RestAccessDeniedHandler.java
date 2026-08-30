package com.riskplatform.common.security;

import com.riskplatform.common.error.CommonErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * 无权限处理器（R17.2）。
 *
 * <p>已认证但角色不足以访问该接口时，返回 403 与结构化错误体
 * {@code { "code": "SYSTEM.UNAUTHORIZED", "message": "..." }}，与平台统一错误体保持一致。
 */
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        RestAuthEntryPoint.writeUnauthorized(response, HttpServletResponse.SC_FORBIDDEN,
                CommonErrorCode.UNAUTHORIZED.code(), "无访问权限，拒绝该请求");
    }
}

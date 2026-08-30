package com.riskplatform.common.error;

import java.util.Map;

/**
 * 结构化错误响应体：{ code, message, fields? }（design.md / 统一错误响应与异常体系）。
 *
 * <p>{@code fields} 为字段级校验错误（字段名 -> 错误原因），供前端映射到表单项并保留用户输入。
 */
public record ErrorResponse(String code, String message, Map<String, String> fields) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null);
    }

    public static ErrorResponse of(String code, String message, Map<String, String> fields) {
        return new ErrorResponse(code, message, fields);
    }
}

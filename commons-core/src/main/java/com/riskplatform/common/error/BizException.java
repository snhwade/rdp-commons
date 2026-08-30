package com.riskplatform.common.error;

import java.util.Map;

/**
 * 业务异常基类。承载 {@link ErrorCode} 与可选的字段级错误。
 *
 * <p>全局异常处理器据此输出结构化错误体并映射 HTTP 状态。
 */
public class BizException extends RuntimeException {

    private final transient ErrorCode errorCode;
    private final transient Map<String, String> fields;

    public BizException(ErrorCode errorCode) {
        this(errorCode, errorCode.defaultMessage(), null);
    }

    public BizException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public BizException(ErrorCode errorCode, String message, Map<String, String> fields) {
        super(message);
        this.errorCode = errorCode;
        this.fields = fields;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public ErrorResponse toResponse() {
        return ErrorResponse.of(errorCode.code(), getMessage(), fields);
    }

    // —— 便捷工厂 ——

    public static BizException missingField(String field) {
        return new BizException(CommonErrorCode.MISSING_FIELD,
                CommonErrorCode.MISSING_FIELD.defaultMessage(), Map.of(field, "必填"));
    }

    public static BizException duplicate(String message) {
        return new BizException(CommonErrorCode.DUPLICATE, message);
    }

    public static BizException notFound(String message) {
        return new BizException(CommonErrorCode.NOT_FOUND, message);
    }

    public static BizException invalidState(String message) {
        return new BizException(CommonErrorCode.INVALID_STATE, message);
    }
}

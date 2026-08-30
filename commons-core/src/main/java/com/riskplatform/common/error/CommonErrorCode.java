package com.riskplatform.common.error;

/**
 * 平台通用错误码。各服务专有错误码可另行定义实现 {@link ErrorCode}。
 */
public enum CommonErrorCode implements ErrorCode {

    MISSING_FIELD("VALIDATION.MISSING_FIELD", "缺少必填字段", ErrorCategory.VALIDATION),
    INVALID_FIELD("VALIDATION.INVALID_FIELD", "字段校验失败", ErrorCategory.VALIDATION),
    REQUEST_TOO_LARGE("VALIDATION.REQUEST_TOO_LARGE", "请求超出大小限制", ErrorCategory.VALIDATION),

    DUPLICATE("BUSINESS.DUPLICATE", "唯一标识重复", ErrorCategory.BUSINESS),
    NOT_FOUND("BUSINESS.NOT_FOUND", "资源不存在", ErrorCategory.BUSINESS),
    INVALID_STATE("BUSINESS.INVALID_STATE", "状态非法", ErrorCategory.BUSINESS),

    DEPENDENCY_UNAVAILABLE("SYSTEM.DEPENDENCY_UNAVAILABLE", "依赖服务不可用", ErrorCategory.SYSTEM),
    TIMEOUT("SYSTEM.TIMEOUT", "处理超时", ErrorCategory.SYSTEM),
    UNAUTHORIZED("SYSTEM.UNAUTHORIZED", "未授权", ErrorCategory.SYSTEM),
    INTERNAL_ERROR("SYSTEM.INTERNAL_ERROR", "系统内部错误", ErrorCategory.SYSTEM);

    private final String code;
    private final String defaultMessage;
    private final ErrorCategory category;

    CommonErrorCode(String code, String defaultMessage, ErrorCategory category) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.category = category;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String defaultMessage() {
        return defaultMessage;
    }

    @Override
    public ErrorCategory category() {
        return category;
    }
}

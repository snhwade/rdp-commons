package com.riskplatform.common.error;

/**
 * 错误类别（错误码分层，design.md / Error Handling）。
 */
public enum ErrorCategory {
    /** 输入校验类（缺字段、越界、格式错误等）。 */
    VALIDATION,
    /** 业务规则类（唯一性冲突、状态非法、不存在等）。 */
    BUSINESS,
    /** 系统降级类（依赖不可用、超时、内部错误等）。 */
    SYSTEM
}

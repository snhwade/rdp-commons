package com.riskplatform.common.error;

/**
 * 错误码抽象。各服务可实现自有错误码枚举。
 *
 * <p>错误码分层（design.md / Error Handling）：
 * <ul>
 *   <li>输入校验类（VALIDATION）</li>
 *   <li>业务规则类（BUSINESS）</li>
 *   <li>系统降级类（SYSTEM）</li>
 * </ul>
 */
public interface ErrorCode {

    /** 唯一错误码字符串，如 "VALIDATION.MISSING_FIELD"。 */
    String code();

    /** 默认错误消息（可被覆盖）。 */
    String defaultMessage();

    /** 错误类别。 */
    ErrorCategory category();
}

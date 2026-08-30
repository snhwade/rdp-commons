package com.riskplatform.common.error;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 输入校验异常：聚合多个字段级错误，供前端逐项回显并保留用户输入。
 */
public class ValidationException extends BizException {

    public ValidationException(Map<String, String> fields) {
        super(CommonErrorCode.INVALID_FIELD, CommonErrorCode.INVALID_FIELD.defaultMessage(), fields);
    }

    public ValidationException(String message, Map<String, String> fields) {
        super(CommonErrorCode.INVALID_FIELD, message, fields);
    }

    /** 构建器，便于累积多个字段错误。 */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, String> fields = new LinkedHashMap<>();

        public Builder field(String name, String reason) {
            fields.put(name, reason);
            return this;
        }

        public boolean hasErrors() {
            return !fields.isEmpty();
        }

        public ValidationException build() {
            return new ValidationException(fields);
        }

        /** 若存在错误则抛出，否则不做任何事。 */
        public void throwIfAny() {
            if (hasErrors()) {
                throw build();
            }
        }
    }
}

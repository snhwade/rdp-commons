package com.riskplatform.common.error;

import com.riskplatform.common.util.RetryExecutor;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * commons-core 公共内核单元测试：错误体、异常工厂、校验聚合、重试封装。
 */
class BizExceptionTest {

    @Test
    void bizException_toResponse_carriesCodeAndMessage() {
        BizException ex = BizException.duplicate("code 重复");
        ErrorResponse resp = ex.toResponse();
        assertThat(resp.code()).isEqualTo(CommonErrorCode.DUPLICATE.code());
        assertThat(resp.message()).isEqualTo("code 重复");
        assertThat(resp.fields()).isNull();
    }

    @Test
    void validationException_aggregatesFieldErrors() {
        ValidationException.Builder b = ValidationException.builder()
                .field("name", "必填")
                .field("code", "格式非法");
        assertThat(b.hasErrors()).isTrue();
        ValidationException ex = b.build();
        assertThat(ex.getFields()).containsEntry("name", "必填").containsEntry("code", "格式非法");
        assertThat(ex.getErrorCode().category()).isEqualTo(ErrorCategory.VALIDATION);
    }

    @Test
    void validationBuilder_throwIfAny_throwsWhenErrorsPresent() {
        assertThatThrownBy(() -> ValidationException.builder().field("x", "bad").throwIfAny())
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void retryExecutor_succeedsAfterRetries() {
        AtomicInteger attempts = new AtomicInteger();
        String result = RetryExecutor.execute(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new RuntimeException("transient");
            }
            return "ok";
        }, 3, 1L);
        assertThat(result).isEqualTo("ok");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void retryExecutor_exhausts_andThrows() {
        AtomicInteger attempts = new AtomicInteger();
        assertThatThrownBy(() -> RetryExecutor.execute(() -> {
            attempts.incrementAndGet();
            throw new RuntimeException("always fail");
        }, 3, 1L)).isInstanceOf(RetryExecutor.RetryExhaustedException.class);
        assertThat(attempts.get()).isEqualTo(3);
    }
}

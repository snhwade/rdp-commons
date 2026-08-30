package com.riskplatform.common.web;

import com.riskplatform.common.error.BizException;
import com.riskplatform.common.error.CommonErrorCode;
import com.riskplatform.common.error.ErrorCategory;
import com.riskplatform.common.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局异常处理器：输出结构化错误体 {@link ErrorResponse}，不泄露内部堆栈。
 *
 * <p>各服务通过组件扫描或显式 Import 启用。HTTP 状态按错误类别映射：
 * VALIDATION→400，BUSINESS→409/404（按码细分），SYSTEM→503/500/401。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ErrorResponse> handleBiz(BizException ex) {
        return ResponseEntity.status(mapStatus(ex)).body(ex.toResponse());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fields.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        ErrorResponse body = ErrorResponse.of(
                CommonErrorCode.INVALID_FIELD.code(),
                CommonErrorCode.INVALID_FIELD.defaultMessage(),
                fields);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() == null
                ? ex.getMessage()
                : ex.getMostSpecificCause().getMessage();
        if (message != null && (message.contains("Duplicate") || message.contains("duplicate")
                || message.contains("uk_"))) {
            ErrorResponse body = ErrorResponse.of(
                    CommonErrorCode.DUPLICATE.code(),
                    CommonErrorCode.DUPLICATE.defaultMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }
        ErrorResponse body = ErrorResponse.of(
                CommonErrorCode.INVALID_FIELD.code(),
                CommonErrorCode.INVALID_FIELD.defaultMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        // 不泄露堆栈，仅返回通用内部错误
        ErrorResponse body = ErrorResponse.of(
                CommonErrorCode.INTERNAL_ERROR.code(),
                CommonErrorCode.INTERNAL_ERROR.defaultMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private HttpStatus mapStatus(BizException ex) {
        ErrorCategory category = ex.getErrorCode().category();
        String code = ex.getErrorCode().code();
        return switch (category) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case BUSINESS -> {
                if (code.endsWith("NOT_FOUND")) {
                    yield HttpStatus.NOT_FOUND;
                }
                yield HttpStatus.CONFLICT;
            }
            case SYSTEM -> {
                if (code.endsWith("UNAUTHORIZED")) {
                    yield HttpStatus.UNAUTHORIZED;
                }
                if (code.endsWith("DEPENDENCY_UNAVAILABLE") || code.endsWith("TIMEOUT")) {
                    yield HttpStatus.SERVICE_UNAVAILABLE;
                }
                yield HttpStatus.INTERNAL_SERVER_ERROR;
            }
        };
    }
}

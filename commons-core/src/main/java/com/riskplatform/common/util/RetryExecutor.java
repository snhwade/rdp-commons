package com.riskplatform.common.util;

import java.util.function.Supplier;

/**
 * 轻量重试封装：最多重试 N 次 + 指数退避。
 *
 * <p>用于 Redis/ES/MySQL 写、AI 指标写等场景（design.md / Error Handling，默认最多 3 次）。
 * 复杂场景可改用 Resilience4j；本工具提供领域内核级的零依赖兜底实现。
 */
public final class RetryExecutor {

    private RetryExecutor() {
    }

    /**
     * 执行带重试的操作。
     *
     * @param action       业务操作
     * @param maxAttempts  最大尝试次数（含首次），范围建议 1..10
     * @param baseBackoffMs 首次退避毫秒（指数增长：base * 2^(attempt-1)）
     * @param <T>          返回类型
     * @return 操作结果
     * @throws RetryExhaustedException 所有尝试失败后抛出，携带最后一次异常
     */
    public static <T> T execute(Supplier<T> action, int maxAttempts, long baseBackoffMs) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts 必须 >= 1");
        }
        RuntimeException last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (RuntimeException e) {
                last = e;
                if (attempt < maxAttempts) {
                    sleep(baseBackoffMs * (1L << (attempt - 1)));
                }
            }
        }
        throw new RetryExhaustedException(maxAttempts, last);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RetryExhaustedException(0, ie);
        }
    }

    /** 重试耗尽异常，携带最后一次失败原因，供调用方记录与告警。 */
    public static class RetryExhaustedException extends RuntimeException {
        private final int attempts;

        public RetryExhaustedException(int attempts, Throwable cause) {
            super("重试耗尽，已尝试 " + attempts + " 次", cause);
            this.attempts = attempts;
        }

        public int getAttempts() {
            return attempts;
        }
    }
}

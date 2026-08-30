package com.riskplatform.common.model;

import java.io.Serializable;

/**
 * Flink 指标累计后下发的切片增量事件（Kafka {@code indicator-slice-updates}）。
 *
 * <p>消费方可按 {@link #increment()} 对 {@link #sliceKey()} 做原子累加，
 * 并依据 {@link #ttlSeconds()} 设置窗口老化 TTL。幂等由 Flink 侧去重保证，
 * 同一 (refName, dimensionKey, orderId) 仅下发一次。
 */
public record IndicatorSliceUpdate(
        String refName,
        String dimensionKey,
        String granularity,
        long sliceTs,
        double increment,
        String orderId,
        String sliceKey,
        long ttlSeconds
) implements Serializable {
}

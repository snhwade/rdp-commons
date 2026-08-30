package com.riskplatform.common.model;

import java.util.List;

/**
 * 统一分页结构。
 *
 * @param data     当前页数据（无结果时为空列表）
 * @param page     当前页码（从 1 开始）
 * @param pageSize 每页大小
 * @param total    总条数
 */
public record PagedResult<T>(List<T> data, int page, int pageSize, long total) {

    public static <T> PagedResult<T> of(List<T> data, int page, int pageSize, long total) {
        return new PagedResult<>(data, page, pageSize, total);
    }

    /** 空结果（用于无匹配时返回空列表，而非 null）。 */
    public static <T> PagedResult<T> empty(int page, int pageSize) {
        return new PagedResult<>(List.of(), page, pageSize, 0L);
    }
}

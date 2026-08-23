package com.mystikos.common.result;

import java.util.List;

/**
 * 通用分页结果封装，不绑定具体分页实现（PageHelper/MyBatis-Plus IPage 都可以转成这个）。
 *
 * @param <T> 单条记录类型
 */
public record PageResult<T>(
        List<T> records,
        long total,
        long pageNum,
        long pageSize,
        long pages
) {
    public static <T> PageResult<T> of(List<T> records, long total, long pageNum, long pageSize) {
        long pages = pageSize <= 0 ? 0 : (total + pageSize - 1) / pageSize;
        return new PageResult<>(records, total, pageNum, pageSize, pages);
    }
}

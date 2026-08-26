package com.mystikos.commerce.application.command;

import java.math.BigDecimal;
import java.util.List;

/** images 是先调用通用文件上传接口（{@code POST /api/v1/files/upload}）拿到的 objectKey/URL 列表。 */
public record CreateProductCommand(
        Long categoryId,
        String name,
        String description,
        BigDecimal price,
        List<String> images,
        int initialStock
) {
}

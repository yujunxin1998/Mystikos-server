package com.mystikos.commerce.application.command;

import com.mystikos.commerce.domain.model.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

/** 后台编辑商品，整体覆盖式更新；status 为空则保持原状态不变。 */
public record UpdateProductCommand(
        Long categoryId,
        String name,
        String description,
        BigDecimal price,
        List<String> images,
        ProductStatus status
) {
}

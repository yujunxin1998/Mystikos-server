package com.mystikos.commerce.domain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品聚合根。陪玩推荐关联（recommendedBy/eligibilityRule，需陪玩确认授权）这次不做，
 * 见 docs/architecture/prd-alignment.md 的缺口清单——先只做普通商品目录。
 */
public class Product {

    private final Long id;
    private final Long categoryId;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final List<String> images;
    private ProductStatus status;

    public Product(Long id, Long categoryId, String name, String description,
                   BigDecimal price, List<String> images, ProductStatus status) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.images = images;
        this.status = status;
    }

    public boolean isOnShelf() {
        return status == ProductStatus.ON_SHELF;
    }

    public Long getId() {
        return id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public List<String> getImages() {
        return images;
    }

    public ProductStatus getStatus() {
        return status;
    }
}

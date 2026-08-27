package com.mystikos.commerce.domain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品聚合根。陪玩推荐关联（recommendedBy/eligibilityRule，需陪玩确认授权）这次不做，
 * 见 docs/architecture/prd-alignment.md 的缺口清单——先只做普通商品目录。
 */
public class Product {

    private Long id;
    private Long categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private List<String> images;
    private ProductStatus status;

    private Product(Long id, Long categoryId, String name, String description,
                     BigDecimal price, List<String> images, ProductStatus status) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.images = images;
        this.status = status;
    }

    /** 后台新增商品，默认直接上架。 */
    public static Product create(Long categoryId, String name, String description,
                                  BigDecimal price, List<String> images) {
        return new Product(null, categoryId, name, description, price,
                images == null ? List.of() : images, ProductStatus.ON_SHELF);
    }

    /** 从持久化数据重建，仅供仓储实现调用。 */
    public static Product restore(Long id, Long categoryId, String name, String description,
                                   BigDecimal price, List<String> images, ProductStatus status) {
        return new Product(id, categoryId, name, description, price, images, status);
    }

    /** 后台编辑商品：整体覆盖式更新基础信息，不涉及库存。 */
    public void updateDetails(Long categoryId, String name, String description,
                               BigDecimal price, List<String> images) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.images = images == null ? List.of() : images;
    }

    public void changeStatus(ProductStatus status) {
        this.status = status;
    }

    public boolean isOnShelf() {
        return status == ProductStatus.ON_SHELF;
    }

    public Long getId() {
        return id;
    }

    /** 仅供仓储实现在插入后回填生成的主键。 */
    public void assignId(Long id) {
        this.id = id;
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

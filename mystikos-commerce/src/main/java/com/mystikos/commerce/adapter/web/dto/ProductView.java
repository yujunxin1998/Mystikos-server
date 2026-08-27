package com.mystikos.commerce.adapter.web.dto;

import com.mystikos.commerce.domain.model.Product;
import com.mystikos.commerce.domain.model.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "商品视图")
public class ProductView implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "商品名")
    private String name;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "图片URL列表")
    private List<String> images;

    @Schema(description = "上下架状态")
    private ProductStatus status;

    public static ProductView from(Product product) {
        ProductView view = new ProductView();
        view.setId(product.getId());
        view.setCategoryId(product.getCategoryId());
        view.setName(product.getName());
        view.setDescription(product.getDescription());
        view.setPrice(product.getPrice());
        view.setImages(product.getImages());
        view.setStatus(product.getStatus());
        return view;
    }
}

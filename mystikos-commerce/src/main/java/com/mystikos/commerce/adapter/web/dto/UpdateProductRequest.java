package com.mystikos.commerce.adapter.web.dto;

import com.mystikos.commerce.domain.model.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 后台编辑商品请求，整体覆盖式更新；不涉及库存调整。 */
@Data
@Schema(description = "后台编辑商品请求")
public class UpdateProductRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "分类ID，暂无分类目录约束，先允许为空")
    private Long categoryId;

    @Schema(description = "商品名")
    @NotBlank
    private String name;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "价格")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "价格必须大于0")
    private BigDecimal price;

    @Schema(description = "图片URL/objectKey列表")
    private List<String> images;

    @Schema(description = "上下架状态，不传则保持原状态")
    private ProductStatus status;
}

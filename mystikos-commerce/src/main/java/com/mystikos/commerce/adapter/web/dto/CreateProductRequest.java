package com.mystikos.commerce.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 后台新增商品请求。图片先调用通用文件上传接口（{@code POST /api/v1/files/upload}）逐张上传，
 * 拿到的 objectKey/URL 填进 {@code images}，这里不直接接收 multipart 文件。
 */
@Data
@Schema(description = "后台新增商品请求")
public class CreateProductRequest implements Serializable {

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

    @Schema(description = "图片URL/objectKey列表，先调用 POST /api/v1/files/upload 逐张上传获取")
    private List<String> images;

    @Schema(description = "初始库存数量")
    @NotNull
    @Min(value = 0, message = "初始库存不能为负数")
    private Integer initialStock;
}

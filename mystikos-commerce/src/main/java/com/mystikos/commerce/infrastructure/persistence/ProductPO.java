package com.mystikos.commerce.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "商品持久化对象")
@TableName("commerce_product")
public class ProductPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "分类ID")
    @TableField("category_id")
    private Long categoryId;

    @Schema(description = "商品名")
    @TableField("name")
    private String name;

    @Schema(description = "商品描述")
    @TableField("description")
    private String description;

    @Schema(description = "价格")
    @TableField("price")
    private BigDecimal price;

    /** 逗号分隔的图片 URL 列表，不建独立表——图片数量少，没有单独查询/排序的需求。 */
    @Schema(description = "图片URL列表，逗号分隔")
    @TableField("images")
    private String images;

    @Schema(description = "上下架状态")
    @TableField("status")
    private String status;
}

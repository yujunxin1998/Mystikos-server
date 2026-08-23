package com.mystikos.commerce.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "库存持久化对象")
@TableName("commerce_inventory_stock")
public class InventoryStockPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID，主键（一个商品一行库存，值由调用方指定，不自动生成）")
    @TableId(type = IdType.INPUT)
    private Long productId;

    @Schema(description = "可售数量")
    @TableField("available_qty")
    private Integer availableQty;

    @Schema(description = "已预占数量")
    @TableField("reserved_qty")
    private Integer reservedQty;
}

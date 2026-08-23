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
@Schema(description = "订单行快照持久化对象")
@TableName("commerce_order_item")
public class OrderItemPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "订单ID")
    @TableField("order_id")
    private Long orderId;

    @Schema(description = "商品ID")
    @TableField("product_id")
    private Long productId;

    @Schema(description = "下单时的商品名快照")
    @TableField("product_name_snapshot")
    private String productNameSnapshot;

    @Schema(description = "下单时的单价快照")
    @TableField("unit_price_snapshot")
    private BigDecimal unitPriceSnapshot;

    @Schema(description = "数量")
    @TableField("quantity")
    private Integer quantity;
}

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
import java.time.OffsetDateTime;

@Data
@Schema(description = "商城订单持久化对象")
@TableName("commerce_order")
public class MerchandiseOrderPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "老板用户ID")
    @TableField("patron_id")
    private Long patronId;

    @Schema(description = "订单总金额")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @Schema(description = "收货地址")
    @TableField("shipping_address")
    private String shippingAddress;

    @Schema(description = "下单时选择的地址ID，仅用于追溯")
    @TableField("shipping_address_id")
    private Long shippingAddressId;

    @Schema(description = "订单状态")
    @TableField("status")
    private String status;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private OffsetDateTime createdAt;
}

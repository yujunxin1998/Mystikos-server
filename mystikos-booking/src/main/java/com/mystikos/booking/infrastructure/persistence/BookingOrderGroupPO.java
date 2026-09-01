package com.mystikos.booking.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Schema(description = "预约组持久化对象")
@TableName("booking_order_group")
public class BookingOrderGroupPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "老板用户ID")
    @TableField("patron_id")
    private Long patronId;

    @Schema(description = "预约组状态：DRAFT/PENDING_PAYMENT/PAID/EXPIRED/CANCELLED")
    @TableField("status")
    private String status;

    @Schema(description = "合计金额")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private OffsetDateTime createdAt;

    @Schema(description = "乐观锁版本号")
    @Version
    @TableField("version")
    private Long version;
}

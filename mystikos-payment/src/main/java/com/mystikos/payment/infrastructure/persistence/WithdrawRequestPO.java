package com.mystikos.payment.infrastructure.persistence;

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
@Schema(description = "陪玩提现申请持久化对象")
@TableName("payment_withdraw_request")
public class WithdrawRequestPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("companion_id")
    private Long companionId;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("currency")
    private String currency;

    @TableField("status")
    private String status;

    @TableField("stripe_transfer_ref")
    private String stripeTransferRef;

    @TableField("decided_by")
    private Long decidedBy;

    @TableField("decided_at")
    private OffsetDateTime decidedAt;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("requested_at")
    private OffsetDateTime requestedAt;
}

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
@Schema(description = "账本行持久化对象，append-only")
@TableName("payment_ledger_entry")
public class LedgerEntryPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("intent_id")
    private Long intentId;

    @TableField("wallet_id")
    private Long walletId;

    @TableField("direction")
    private String direction;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("currency")
    private String currency;

    @TableField("occurred_at")
    private OffsetDateTime occurredAt;
}

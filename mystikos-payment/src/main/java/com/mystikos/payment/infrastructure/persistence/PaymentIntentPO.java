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
@Schema(description = "支付意图持久化对象")
@TableName("payment_intent")
public class PaymentIntentPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("source_type")
    private String sourceType;

    @TableField("source_id")
    private Long sourceId;

    @TableField("patron_id")
    private Long patronId;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("currency")
    private String currency;

    @TableField("status")
    private String status;

    @TableField("gateway_provider")
    private String gatewayProvider;

    @TableField("gateway_ref")
    private String gatewayRef;

    @TableField("client_secret")
    private String clientSecret;

    @TableField("idempotency_key")
    private String idempotencyKey;

    @TableField("failure_reason")
    private String failureReason;

    @TableField("created_at")
    private OffsetDateTime createdAt;

    @TableField("updated_at")
    private OffsetDateTime updatedAt;
}

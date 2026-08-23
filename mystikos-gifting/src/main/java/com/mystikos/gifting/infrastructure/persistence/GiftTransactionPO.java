package com.mystikos.gifting.infrastructure.persistence;

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
@Schema(description = "赠礼流水持久化对象")
@TableName("gifting_transaction")
public class GiftTransactionPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "赠送方（老板）用户ID")
    @TableField("patron_id")
    private Long patronId;

    @Schema(description = "接收方（陪玩）用户ID")
    @TableField("companion_id")
    private Long companionId;

    @Schema(description = "礼物ID")
    @TableField("gift_id")
    private Long giftId;

    @Schema(description = "赠送数量")
    @TableField("quantity")
    private Integer quantity;

    @Schema(description = "本次交易总金额")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "赠送时间")
    @TableField("sent_at")
    private OffsetDateTime sentAt;
}

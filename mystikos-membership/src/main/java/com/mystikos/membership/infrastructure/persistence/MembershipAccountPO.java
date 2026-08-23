package com.mystikos.membership.infrastructure.persistence;

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
@Schema(description = "会员成长账户持久化对象")
@TableName("membership_account")
public class MembershipAccountPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "老板用户ID")
    @TableField("patron_id")
    private Long patronId;

    @Schema(description = "当前等级编码")
    @TableField("current_tier_code")
    private String currentTierCode;

    @Schema(description = "累计消费金额")
    @TableField("cumulative_spend")
    private BigDecimal cumulativeSpend;

    @Schema(description = "最近一次升级时间")
    @TableField("tier_upgraded_at")
    private OffsetDateTime tierUpgradedAt;
}

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

@Data
@Schema(description = "礼物目录持久化对象")
@TableName("gifting_catalog_item")
public class GiftCatalogItemPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "礼物编码")
    @TableField("code")
    private String code;

    @Schema(description = "礼物展示名")
    @TableField("name")
    private String name;

    @Schema(description = "图标标识/URL")
    @TableField("icon")
    private String icon;

    @Schema(description = "单价")
    @TableField("price")
    private BigDecimal price;

    @Schema(description = "所属档位ID")
    @TableField("tier_id")
    private Long tierId;

    @Schema(description = "解锁规则类型")
    @TableField("unlock_rule_type")
    private String unlockRuleType;

    @Schema(description = "解锁规则阈值")
    @TableField("unlock_rule_threshold")
    private BigDecimal unlockRuleThreshold;

    @Schema(description = "是否上架")
    @TableField("active")
    private Boolean active;
}

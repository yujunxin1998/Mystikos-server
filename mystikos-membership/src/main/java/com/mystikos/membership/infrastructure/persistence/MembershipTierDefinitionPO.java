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

@Data
@Schema(description = "VIP 等级定义持久化对象")
@TableName("membership_tier_definition")
public class MembershipTierDefinitionPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "等级编码")
    @TableField("code")
    private String code;

    @Schema(description = "展示名")
    @TableField("display_name")
    private String displayName;

    @Schema(description = "英文展示名")
    @TableField("display_name_en")
    private String displayNameEn;

    @Schema(description = "对外展示的等级数值")
    @TableField("level")
    private Integer level;

    @Schema(description = "进入该等级所需的最低累计消费")
    @TableField("cumulative_spend_threshold")
    private BigDecimal cumulativeSpendThreshold;

    @Schema(description = "权益文案")
    @TableField("perk_description")
    private String perkDescription;

    @Schema(description = "排序位次")
    @TableField("sort_order")
    private Integer sortOrder;
}

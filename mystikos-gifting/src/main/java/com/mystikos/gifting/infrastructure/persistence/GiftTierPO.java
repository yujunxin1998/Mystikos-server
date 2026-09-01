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
@Schema(description = "礼物档位持久化对象")
@TableName("gifting_tier")
public class GiftTierPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "档位编码")
    @TableField("code")
    private String code;

    @Schema(description = "展示名")
    @TableField("display_name")
    private String displayName;

    @Schema(description = "英文展示名")
    @TableField("display_name_en")
    private String displayNameEn;

    @Schema(description = "亲密度倍率")
    @TableField("multiplier")
    private BigDecimal multiplier;

    @Schema(description = "排序位次")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "是否启用")
    @TableField("active")
    private Boolean active;
}

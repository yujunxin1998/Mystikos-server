package com.mystikos.relationship.infrastructure.persistence;

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
@Schema(description = "亲密度等级定义持久化对象")
@TableName("relationship_intimacy_level_definition")
public class IntimacyLevelDefinitionPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "等级编码")
    @TableField("code")
    private String code;

    @Schema(description = "中文展示名")
    @TableField("display_name_zh")
    private String displayNameZh;

    @Schema(description = "英文展示名")
    @TableField("display_name_en")
    private String displayNameEn;

    @Schema(description = "进入该等级所需的最低累计进度")
    @TableField("threshold")
    private BigDecimal threshold;

    @Schema(description = "权益文案")
    @TableField("perk_description")
    private String perkDescription;

    @Schema(description = "排序位次")
    @TableField("sort_order")
    private Integer sortOrder;
}

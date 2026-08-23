package com.mystikos.identity.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 标签定义持久化对象
 * @author mystikos
 */
@Data
@Schema(description = "标签定义持久化对象")
@TableName("identity_tag_definition")
public class TagDefinitionPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 标签分类（如 GAME_TYPE），自由字符串，后台配置
     */
    @Schema(description = "标签分类")
    @TableField("category")
    private String category;

    /**
     * 标签展示名
     */
    @Schema(description = "标签展示名")
    @TableField("label")
    private String label;

    /**
     * 排序权重，越小越靠前
     */
    @Schema(description = "排序权重")
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 是否启用，停用后不出现在前台多选列表里
     */
    @Schema(description = "是否启用")
    @TableField("enabled")
    private Boolean enabled;
}

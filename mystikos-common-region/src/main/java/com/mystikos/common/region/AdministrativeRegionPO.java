package com.mystikos.common.region;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 行政区划持久化对象。主键是 ISO 3166-1/3166-2 编码本身（如 "DE"、"DE-BY"），
 * 不用雪花算法生成——这是稳定的自然键，用它当主键比额外造一个代理ID更直接。
 * @author mystikos
 */
@Data
@Schema(description = "行政区划持久化对象")
@TableName("common_region")
public class AdministrativeRegionPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ISO 3166-1 alpha-2（国家）或 ISO 3166-2（一级行政区）编码，主键
     */
    @Schema(description = "行政区划编码")
    @TableId(type = IdType.INPUT)
    private String code;

    /**
     * 上级编码，国家级为 NULL，一级行政区为所属国家的 code
     */
    @Schema(description = "上级编码")
    @TableField("parent_code")
    private String parentCode;

    /**
     * 层级：COUNTRY / SUBDIVISION
     */
    @Schema(description = "层级")
    @TableField("level")
    private String level;

    /**
     * 中文展示名
     */
    @Schema(description = "中文展示名")
    @TableField("name_zh")
    private String nameZh;

    /**
     * 英文展示名
     */
    @Schema(description = "英文展示名")
    @TableField("name_en")
    private String nameEn;

    /**
     * 排序权重，越小越靠前
     */
    @Schema(description = "排序权重")
    @TableField("sort_order")
    private Integer sortOrder;
}

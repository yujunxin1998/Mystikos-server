package com.mystikos.relationship.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/** 单行配置表，主键固定为 1——没有"新增一行配置"这种操作，只有 UPDATE。 */
@Data
@Schema(description = "亲密度模块配置持久化对象")
@TableName("relationship_settings")
public class RelationshipSettingsPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键，固定为1")
    @TableId
    private Long id;

    @Schema(description = "每日亲密度获取上限")
    @TableField("daily_intimacy_cap")
    private BigDecimal dailyIntimacyCap;
}

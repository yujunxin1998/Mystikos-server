package com.mystikos.leaderboard.infrastructure.persistence;

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
@Schema(description = "陪玩魅力值统计持久化对象")
@TableName("leaderboard_companion_stat")
public class CompanionCharmStatPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "代理主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "陪玩用户ID")
    @TableField("companion_id")
    private Long companionId;

    @Schema(description = "累计魅力值")
    @TableField("charm_value")
    private BigDecimal charmValue;
}

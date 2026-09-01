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
import java.time.LocalDate;

@Data
@Schema(description = "亲密度每日累加计数器持久化对象")
@TableName("relationship_intimacy_daily_accrual")
public class IntimacyDailyAccrualPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "代理主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "老板用户ID")
    @TableField("patron_id")
    private Long patronId;

    @Schema(description = "陪玩用户ID")
    @TableField("companion_id")
    private Long companionId;

    @Schema(description = "统计日期")
    @TableField("stat_date")
    private LocalDate statDate;

    @Schema(description = "当日已计入亲密度的累计值")
    @TableField("accrued")
    private BigDecimal accrued;
}

package com.mystikos.booking.infrastructure.persistence;

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
@Schema(description = "预约购物车行持久化对象")
@TableName("booking_cart_line")
public class BookingCartLinePO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "老板用户ID")
    @TableField("patron_id")
    private Long patronId;

    @Schema(description = "陪玩用户ID")
    @TableField("companion_id")
    private Long companionId;

    @Schema(description = "预约时段开始时间")
    @TableField("time_range_start")
    private OffsetDateTime timeRangeStart;

    @Schema(description = "预约时段结束时间")
    @TableField("time_range_end")
    private OffsetDateTime timeRangeEnd;

    @Schema(description = "时长（小时）")
    @TableField("duration_hours")
    private BigDecimal durationHours;

    @Schema(description = "加入购物车时间")
    @TableField("created_at")
    private OffsetDateTime createdAt;
}

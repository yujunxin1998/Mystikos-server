package com.mystikos.booking.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 预约订单持久化对象
 * @author mystikos
 */
@Data
@Schema(description = "预约订单持久化对象")
@TableName("booking_order")
public class BookingOrderPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法生成，插入前就已确定——领域事件需要在写库时立即拿到ID，
     * 不用 IdType.AUTO 数据库自增）
     */
    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 下单老板用户ID
     */
    @Schema(description = "下单老板用户ID")
    @TableField("patron_id")
    private Long patronId;

    /**
     * 陪玩用户ID
     */
    @Schema(description = "陪玩用户ID")
    @TableField("companion_id")
    private Long companionId;

    /**
     * 预约时段开始时间
     */
    @Schema(description = "预约时段开始时间")
    @TableField("time_range_start")
    private OffsetDateTime timeRangeStart;

    /**
     * 预约时段结束时间
     */
    @Schema(description = "预约时段结束时间")
    @TableField("time_range_end")
    private OffsetDateTime timeRangeEnd;

    /**
     * 下单时长（小时），用于按小时计费
     */
    @Schema(description = "下单时长（小时）")
    @TableField("duration_hours")
    private BigDecimal durationHours;

    /**
     * 下单时的价格快照
     */
    @Schema(description = "下单时的价格快照")
    @TableField("price_snapshot")
    private BigDecimal priceSnapshot;

    /**
     * 订单状态：DRAFT/PENDING_PAYMENT/PAID/MATCHING/ACCEPTED/IN_SERVICE/COMPLETED/CANCELLED/EXPIRED/DISPUTED/REFUNDED
     */
    @Schema(description = "订单状态")
    @TableField("status")
    private String status;

    /**
     * 下单时刻，15分钟支付有效期从这里起算
     */
    @Schema(description = "下单时刻")
    @TableField("created_at")
    private OffsetDateTime createdAt;

    /**
     * 乐观锁版本号
     */
    @Schema(description = "乐观锁版本号")
    @Version
    @TableField("version")
    private Long version;
}

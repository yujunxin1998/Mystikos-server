package com.mystikos.booking.domain.model;

import java.math.BigDecimal;

/**
 * 预约购物车里的一行草稿：陪玩+开始时间+时长，还没算权威价格、也还没占用数据库层面的
 * 防重占位（那是结算落单那一刻才发生的事，见 BookingApplicationService#checkoutBookingCart）。
 * 不存价格快照——购物车里放久了价格可能过期，列表/结算时都用 CompanionPricingPort 现查。
 */
public class BookingCartLine {

    private Long id;
    private final Long patronId;
    private final Long companionId;
    private final TimeRange timeRange;
    private final BigDecimal durationHours;

    private BookingCartLine(Long id, Long patronId, Long companionId, TimeRange timeRange, BigDecimal durationHours) {
        this.id = id;
        this.patronId = patronId;
        this.companionId = companionId;
        this.timeRange = timeRange;
        this.durationHours = durationHours;
    }

    public static BookingCartLine create(Long patronId, Long companionId, TimeRange timeRange, BigDecimal durationHours) {
        return new BookingCartLine(null, patronId, companionId, timeRange, durationHours);
    }

    public static BookingCartLine restore(Long id, Long patronId, Long companionId, TimeRange timeRange,
                                           BigDecimal durationHours) {
        return new BookingCartLine(id, patronId, companionId, timeRange, durationHours);
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getPatronId() {
        return patronId;
    }

    public Long getCompanionId() {
        return companionId;
    }

    public TimeRange getTimeRange() {
        return timeRange;
    }

    public BigDecimal getDurationHours() {
        return durationHours;
    }
}

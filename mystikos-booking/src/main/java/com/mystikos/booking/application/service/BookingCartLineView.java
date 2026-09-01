package com.mystikos.booking.application.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 预约购物车行视图。estimatedPrice/companionBookable 都是实时查询陪玩当前定价得出的——
 * 购物车里放久了价格可能变化，这里不用存进库里的快照，权威价格在结算落单那一刻才算一次。
 */
public record BookingCartLineView(Long id, Long companionId, OffsetDateTime start, OffsetDateTime end,
                                   BigDecimal durationHours, BigDecimal estimatedPrice, boolean companionBookable) {
}

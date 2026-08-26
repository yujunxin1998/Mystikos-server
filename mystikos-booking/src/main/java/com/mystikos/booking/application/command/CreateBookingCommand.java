package com.mystikos.booking.application.command;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** 下单时只传"陪玩+开始时间+时长"，价格由服务端查陪玩当前时薪权威计算，见 BookingApplicationService。 */
public record CreateBookingCommand(
        Long patronId,
        Long companionId,
        OffsetDateTime start,
        BigDecimal durationHours
) {
}

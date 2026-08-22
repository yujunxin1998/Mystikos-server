package com.mystikos.booking.application.command;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateBookingCommand(
        Long patronId,
        Long companionId,
        Long skuId,
        OffsetDateTime start,
        OffsetDateTime end,
        BigDecimal priceSnapshot
) {
}

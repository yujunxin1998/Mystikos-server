package com.mystikos.booking.application.command;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AddBookingCartLineCommand(Long patronId, Long companionId, OffsetDateTime start, BigDecimal durationHours) {
}

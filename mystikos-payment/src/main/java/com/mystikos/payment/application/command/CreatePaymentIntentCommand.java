package com.mystikos.payment.application.command;

import com.mystikos.payment.domain.model.SourceType;

import java.math.BigDecimal;

public record CreatePaymentIntentCommand(SourceType sourceType, Long sourceId, Long patronId,
                                          BigDecimal amount, String currency) {
}

package com.mystikos.payment.application.command;

import com.mystikos.payment.application.port.PaymentScene;
import com.mystikos.payment.domain.model.PaymentProvider;
import com.mystikos.payment.domain.model.SourceType;

import java.math.BigDecimal;

public record CreatePaymentIntentCommand(SourceType sourceType, Long sourceId, Long patronId,
                                          BigDecimal amount, String currency,
                                          PaymentProvider provider, PaymentScene scene) {

    /** Stripe 走这个，不需要调用方关心 scene。 */
    public static CreatePaymentIntentCommand stripe(SourceType sourceType, Long sourceId, Long patronId,
                                                     BigDecimal amount, String currency) {
        return new CreatePaymentIntentCommand(sourceType, sourceId, patronId, amount, currency,
                PaymentProvider.STRIPE, PaymentScene.DEFAULT);
    }
}

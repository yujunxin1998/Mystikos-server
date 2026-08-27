package com.mystikos.commerce.application.port;

import com.mystikos.payment.application.port.PaymentPayloadType;

import java.util.Map;

public record PaymentCheckoutResult(Long intentId, PaymentPayloadType payloadType, Map<String, String> payload,
                                     String status) {
}

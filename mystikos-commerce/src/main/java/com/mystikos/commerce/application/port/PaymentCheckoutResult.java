package com.mystikos.commerce.application.port;

public record PaymentCheckoutResult(Long intentId, String clientSecret, String status) {
}

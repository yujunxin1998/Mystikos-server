package com.mystikos.booking.application.port;

public record PaymentCheckoutResult(Long intentId, String clientSecret, String status) {
}

package com.mystikos.payment.application.port;

public record GatewayIntentResult(String gatewayRef, String clientSecret) {
}

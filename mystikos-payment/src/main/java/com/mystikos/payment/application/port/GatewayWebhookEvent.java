package com.mystikos.payment.application.port;

public record GatewayWebhookEvent(GatewayEventType type, String gatewayRef, String failureReason) {
}

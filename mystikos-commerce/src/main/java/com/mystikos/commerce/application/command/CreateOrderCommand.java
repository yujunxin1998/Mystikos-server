package com.mystikos.commerce.application.command;

public record CreateOrderCommand(Long patronId, String shippingAddress) {
}

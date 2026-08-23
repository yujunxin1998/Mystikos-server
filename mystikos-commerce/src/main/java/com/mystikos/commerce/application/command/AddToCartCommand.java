package com.mystikos.commerce.application.command;

public record AddToCartCommand(Long patronId, Long productId, int quantity) {
}

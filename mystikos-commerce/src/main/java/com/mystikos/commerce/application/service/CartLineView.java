package com.mystikos.commerce.application.service;

import java.math.BigDecimal;

public record CartLineView(Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal subtotal) {
}

package com.mystikos.commerce.application.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record WishlistLineView(Long productId, String productName, BigDecimal unitPrice, OffsetDateTime addedAt) {
}

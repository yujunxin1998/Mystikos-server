package com.mystikos.commerce.application.command;

/** 立即购买：跳过购物车，直接用商品+数量下单。 */
public record CreateDirectOrderCommand(Long patronId, Long productId, int quantity, Long addressId) {
}

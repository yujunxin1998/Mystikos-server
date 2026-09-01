package com.mystikos.commerce.application.command;

import java.util.List;

/** 用购物车中选中的部分/全部行下单；未列入 productIds 的行继续留在购物车里。 */
public record CreateOrderCommand(Long patronId, List<Long> productIds, Long addressId) {
}

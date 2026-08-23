package com.mystikos.commerce.domain;

import com.mystikos.common.result.IResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Commerce 上下文错误码，号段 6000-6999（见 docs/architecture/exception-handling.md）。
 */
@Getter
@AllArgsConstructor
public enum CommerceResponseCode implements IResponseCode {

    PRODUCT_NOT_FOUND(6001, "商品不存在"),
    PRODUCT_OFF_SHELF(6002, "商品已下架"),
    INSUFFICIENT_STOCK(6003, "库存不足"),
    ORDER_NOT_FOUND(6004, "订单不存在"),
    ORDER_STATUS_INVALID(6005, "当前订单状态不允许该操作"),
    ORDER_EMPTY(6006, "购物车为空，无法下单");

    private final int code;
    private final String message;
}

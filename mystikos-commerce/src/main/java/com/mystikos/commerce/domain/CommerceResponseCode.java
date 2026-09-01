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
    ORDER_EMPTY(6006, "购物车为空，无法下单"),
    ORDER_EXPIRED(6007, "订单已超过支付有效期"),
    CART_LINE_NOT_FOUND(6008, "选中的商品不在购物车中"),
    ADDRESS_NOT_FOUND(6009, "收货地址不存在"),
    ADDRESS_FIELD_INVALID(6010, "地址信息不完整或不合法"),
    REGION_CODE_INVALID(6011, "行政区划编码不存在");

    private final int code;
    private final String message;
}

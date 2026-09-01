package com.mystikos.commerce.domain;

import com.mystikos.common.web.exception.BusinessException;

public class CommerceException extends BusinessException {

    public CommerceException(CommerceResponseCode code) {
        super(code);
    }

    public CommerceException(CommerceResponseCode code, String message) {
        super(code, message);
    }

    public static CommerceException productNotFound(Long productId) {
        return new CommerceException(CommerceResponseCode.PRODUCT_NOT_FOUND, "商品不存在：" + productId);
    }

    public static CommerceException productOffShelf(Long productId) {
        return new CommerceException(CommerceResponseCode.PRODUCT_OFF_SHELF, "商品已下架：" + productId);
    }

    public static CommerceException insufficientStock(Long productId) {
        return new CommerceException(CommerceResponseCode.INSUFFICIENT_STOCK, "库存不足：" + productId);
    }

    public static CommerceException orderNotFound(Long orderId) {
        return new CommerceException(CommerceResponseCode.ORDER_NOT_FOUND, "订单不存在：" + orderId);
    }

    public static CommerceException statusInvalid(String message) {
        return new CommerceException(CommerceResponseCode.ORDER_STATUS_INVALID, message);
    }

    public static CommerceException orderEmpty() {
        return new CommerceException(CommerceResponseCode.ORDER_EMPTY);
    }

    public static CommerceException orderExpired(Long orderId) {
        return new CommerceException(CommerceResponseCode.ORDER_EXPIRED, "订单已失效：" + orderId);
    }

    public static CommerceException cartLineNotFound(Long productId) {
        return new CommerceException(CommerceResponseCode.CART_LINE_NOT_FOUND, "购物车中不存在该商品：" + productId);
    }

    public static CommerceException addressNotFound(Long addressId) {
        return new CommerceException(CommerceResponseCode.ADDRESS_NOT_FOUND, "地址不存在：" + addressId);
    }

    public static CommerceException addressInvalid(String message) {
        return new CommerceException(CommerceResponseCode.ADDRESS_FIELD_INVALID, message);
    }

    public static CommerceException regionCodeInvalid(String code) {
        return new CommerceException(CommerceResponseCode.REGION_CODE_INVALID, "行政区划编码不存在：" + code);
    }
}

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
}

package com.mystikos.booking.domain;

import com.mystikos.common.result.IResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Booking 上下文错误码，号段 5000-5999（见 docs/architecture/exception-handling.md）。
 */
@Getter
@AllArgsConstructor
public enum BookingResponseCode implements IResponseCode {

    SLOT_CONFLICT(5001, "该时段已被预约，请选择其他时间"),
    BOOKING_NOT_FOUND(5002, "预约订单不存在"),
    BOOKING_STATUS_INVALID(5003, "当前订单状态不允许该操作");

    private final int code;
    private final String message;
}

package com.mystikos.booking.domain;

import com.mystikos.common.web.exception.BusinessException;

public class BookingException extends BusinessException {

    public BookingException(BookingResponseCode code) {
        super(code);
    }

    public BookingException(BookingResponseCode code, String message) {
        super(code, message);
    }

    public static BookingException notFound(Long bookingId) {
        return new BookingException(BookingResponseCode.BOOKING_NOT_FOUND,
                "预约订单不存在：" + bookingId);
    }

    public static BookingException statusInvalid(String message) {
        return new BookingException(BookingResponseCode.BOOKING_STATUS_INVALID, message);
    }

    public static BookingException expired(Long bookingId) {
        return new BookingException(BookingResponseCode.BOOKING_EXPIRED,
                "订单已超过15分钟支付有效期：" + bookingId);
    }

    public static BookingException companionNotBookable(Long companionId) {
        return new BookingException(BookingResponseCode.BOOKING_COMPANION_NOT_BOOKABLE,
                "陪玩不存在或当前不可预约：" + companionId);
    }
}

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
}

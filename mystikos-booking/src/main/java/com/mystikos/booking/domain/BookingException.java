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

    public static BookingException slotConflict(Long companionId) {
        return new BookingException(BookingResponseCode.SLOT_CONFLICT, "该陪玩在此时段已被预约：" + companionId);
    }

    /** 批量结算时不逐行区分是哪个陪玩/时段冲突，用这个不带 ID 的版本。 */
    public static BookingException slotConflict() {
        return new BookingException(BookingResponseCode.SLOT_CONFLICT);
    }

    public static BookingException cartLineNotFound(Long lineId) {
        return new BookingException(BookingResponseCode.BOOKING_NOT_FOUND, "预约购物车中不存在该行：" + lineId);
    }

    public static BookingException groupNotFound(Long groupId) {
        return new BookingException(BookingResponseCode.BOOKING_NOT_FOUND, "预约组不存在：" + groupId);
    }
}

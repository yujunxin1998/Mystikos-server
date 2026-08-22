package com.mystikos.booking.domain.repository;

import com.mystikos.booking.domain.model.BookingOrder;

import java.util.Optional;

/**
 * 领域层只定义接口，不依赖 MyBatis-Plus；实现在
 * infrastructure/persistence 包（BookingRepositoryImpl）。
 */
public interface BookingRepository {

    BookingOrder save(BookingOrder bookingOrder);

    Optional<BookingOrder> findById(Long id);
}

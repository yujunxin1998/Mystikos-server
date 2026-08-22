package com.mystikos.booking.application.service;

import com.mystikos.booking.application.command.CreateBookingCommand;
import com.mystikos.booking.domain.event.BookingCreatedEvent;
import com.mystikos.booking.domain.model.BookingOrder;
import com.mystikos.booking.domain.model.TimeRange;
import com.mystikos.booking.domain.repository.BookingRepository;
import com.mystikos.common.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 预约撮合用例编排。跨限界上下文的调用（校验陪玩定价/档期、发起支付）
 * 应经 application/port 接口对接 Provider Catalog、Payment 模块；
 * 这两个模块尚未落地，先只实现创建预约这一步，用例补全时再加 Port。
 */
@Service
public class BookingApplicationService {

    private final BookingRepository bookingRepository;
    private final DomainEventPublisher eventPublisher;

    public BookingApplicationService(BookingRepository bookingRepository,
                                      DomainEventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long createBooking(CreateBookingCommand command) {
        BookingOrder order = BookingOrder.create(
                command.patronId(),
                command.companionId(),
                command.skuId(),
                new TimeRange(command.start(), command.end()),
                command.priceSnapshot());

        BookingOrder saved = bookingRepository.save(order);
        eventPublisher.publish(new BookingCreatedEvent(
                saved.getId(), saved.getPatronId(), saved.getCompanionId()));
        return saved.getId();
    }
}

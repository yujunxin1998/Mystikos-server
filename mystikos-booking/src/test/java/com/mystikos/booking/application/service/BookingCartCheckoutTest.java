package com.mystikos.booking.application.service;

import com.mystikos.booking.application.command.AddBookingCartLineCommand;
import com.mystikos.booking.application.port.CompanionPricingPort;
import com.mystikos.booking.application.port.CompanionPricingSnapshot;
import com.mystikos.booking.application.port.PaymentCheckoutResult;
import com.mystikos.booking.application.port.PaymentPort;
import com.mystikos.booking.domain.BookingException;
import com.mystikos.booking.domain.model.BookingCartLine;
import com.mystikos.booking.domain.model.BookingGroupStatus;
import com.mystikos.booking.domain.model.BookingOrder;
import com.mystikos.booking.domain.model.BookingOrderGroup;
import com.mystikos.booking.domain.model.BookingStatus;
import com.mystikos.booking.domain.model.TimeRange;
import com.mystikos.booking.domain.repository.BookingCartLineRepository;
import com.mystikos.booking.domain.repository.BookingOrderGroupRepository;
import com.mystikos.booking.domain.repository.BookingRepository;
import com.mystikos.common.event.DomainEventPublisher;
import com.mystikos.payment.application.port.PaymentPayloadType;
import com.mystikos.payment.application.port.PaymentScene;
import com.mystikos.payment.domain.model.PaymentProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 纯 Mockito 单元测试，不起 Spring 容器——覆盖预约购物车结算合并成组、组合支付只发起一次
 * 支付请求（而不是逐条预约各发一次）、组状态级联到子预约、以及组的归属校验这几条新行为。
 */
class BookingCartCheckoutTest {

    private static final Long PATRON_ID = 1001L;
    private static final Long OTHER_PATRON_ID = 2002L;

    private BookingRepository bookingRepository;
    private BookingCartLineRepository bookingCartLineRepository;
    private BookingOrderGroupRepository bookingOrderGroupRepository;
    private PaymentPort paymentPort;
    private CompanionPricingPort companionPricingPort;
    private BookingApplicationService service;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        bookingCartLineRepository = mock(BookingCartLineRepository.class);
        bookingOrderGroupRepository = mock(BookingOrderGroupRepository.class);
        DomainEventPublisher eventPublisher = mock(DomainEventPublisher.class);
        paymentPort = mock(PaymentPort.class);
        companionPricingPort = mock(CompanionPricingPort.class);

        service = new BookingApplicationService(bookingRepository, bookingCartLineRepository,
                bookingOrderGroupRepository, eventPublisher, paymentPort, companionPricingPort);

        AtomicLong bookingIdSequence = new AtomicLong(300);
        when(bookingRepository.save(any())).thenAnswer(invocation -> {
            BookingOrder order = invocation.getArgument(0);
            if (order.getId() == null) {
                order.assignId(bookingIdSequence.getAndIncrement());
            }
            return order;
        });
        AtomicLong groupIdSequence = new AtomicLong(400);
        when(bookingOrderGroupRepository.save(any())).thenAnswer(invocation -> {
            BookingOrderGroup group = invocation.getArgument(0);
            if (group.getId() == null) {
                group.assignId(groupIdSequence.getAndIncrement());
            }
            return group;
        });
    }

    @Test
    void checkoutCombinesSelectedLinesIntoOneGroupWithAllChildBookings() {
        BookingCartLine lineA = BookingCartLine.restore(10L, PATRON_ID, 501L,
                new TimeRange(OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(2)),
                new BigDecimal("2.0"));
        BookingCartLine lineB = BookingCartLine.restore(11L, PATRON_ID, 502L,
                new TimeRange(OffsetDateTime.now().plusDays(2), OffsetDateTime.now().plusDays(2).plusHours(3)),
                new BigDecimal("3.0"));
        when(bookingCartLineRepository.findByIdsAndPatron(PATRON_ID, List.of(10L, 11L)))
                .thenReturn(List.of(lineA, lineB));
        when(companionPricingPort.getPricing(501L)).thenReturn(new CompanionPricingSnapshot(new BigDecimal("100.00"), true));
        when(companionPricingPort.getPricing(502L)).thenReturn(new CompanionPricingSnapshot(new BigDecimal("50.00"), true));

        Long groupId = service.checkoutBookingCart(PATRON_ID, List.of(10L, 11L));

        assertThat(groupId).isNotNull();
        verify(bookingRepository, times(2)).save(any());
        verify(bookingCartLineRepository).deleteByPatronAndIds(PATRON_ID, List.of(10L, 11L));
    }

    @Test
    void checkoutRejectsLineIdNotOwnedByPatron() {
        when(bookingCartLineRepository.findByIdsAndPatron(PATRON_ID, List.of(10L))).thenReturn(List.of());

        assertThatThrownBy(() -> service.checkoutBookingCart(PATRON_ID, List.of(10L)))
                .isInstanceOf(BookingException.class);
    }

    @Test
    void requestGroupPaymentIssuesOnlyOnePaymentCallForTheWholeGroup() {
        BookingOrderGroup group = BookingOrderGroup.restore(400L, PATRON_ID, BookingGroupStatus.DRAFT,
                new BigDecimal("350.00"), OffsetDateTime.now(), 0L);
        when(bookingOrderGroupRepository.findById(400L)).thenReturn(Optional.of(group));
        BookingOrder childA = groupChild(310L, 400L, BookingStatus.DRAFT);
        BookingOrder childB = groupChild(311L, 400L, BookingStatus.DRAFT);
        when(bookingRepository.findByGroupId(400L)).thenReturn(List.of(childA, childB));
        when(paymentPort.requestGroupPayment(eq(400L), eq(PATRON_ID), any(), any(), any(), any()))
                .thenReturn(new PaymentCheckoutResult(999L, PaymentPayloadType.QR_CODE, Map.of("qrCode", "x"), "CREATED"));

        service.requestGroupPayment(400L, PATRON_ID, PaymentProvider.ALIPAY, PaymentScene.PC_QR);

        verify(paymentPort, times(1)).requestGroupPayment(anyLong(), anyLong(), any(), any(), any(), any());
        assertThat(group.getStatus()).isEqualTo(BookingGroupStatus.PENDING_PAYMENT);
        assertThat(childA.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(childB.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void markGroupPaidCascadesToAllChildBookings() {
        BookingOrderGroup group = BookingOrderGroup.restore(400L, PATRON_ID, BookingGroupStatus.PENDING_PAYMENT,
                new BigDecimal("350.00"), OffsetDateTime.now(), 0L);
        when(bookingOrderGroupRepository.findById(400L)).thenReturn(Optional.of(group));
        BookingOrder childA = groupChild(310L, 400L, BookingStatus.PENDING_PAYMENT);
        BookingOrder childB = groupChild(311L, 400L, BookingStatus.PENDING_PAYMENT);
        when(bookingRepository.findByGroupId(400L)).thenReturn(List.of(childA, childB));

        service.markGroupPaid(400L);

        assertThat(group.getStatus()).isEqualTo(BookingGroupStatus.PAID);
        assertThat(childA.getStatus()).isEqualTo(BookingStatus.PAID);
        assertThat(childB.getStatus()).isEqualTo(BookingStatus.PAID);
    }

    @Test
    void expireOverdueGroupsCascadesExpiryToChildBookingsStillUnpaid() {
        OffsetDateTime staleCreatedAt = OffsetDateTime.now().minus(BookingOrderGroup.PAYMENT_VALIDITY).minusMinutes(1);
        BookingOrderGroup overdueGroup = BookingOrderGroup.restore(400L, PATRON_ID, BookingGroupStatus.PENDING_PAYMENT,
                new BigDecimal("350.00"), staleCreatedAt, 0L);
        when(bookingOrderGroupRepository.findExpirableGroups(any())).thenReturn(List.of(overdueGroup));
        BookingOrder childA = groupChild(310L, 400L, BookingStatus.PENDING_PAYMENT);
        when(bookingRepository.findByGroupId(400L)).thenReturn(List.of(childA));

        service.expireOverdueGroups();

        assertThat(overdueGroup.getStatus()).isEqualTo(BookingGroupStatus.EXPIRED);
        assertThat(childA.getStatus()).isEqualTo(BookingStatus.EXPIRED);
    }

    @Test
    void accessingSomeoneElsesGroupIsTreatedAsNotFound() {
        BookingOrderGroup group = BookingOrderGroup.restore(400L, OTHER_PATRON_ID, BookingGroupStatus.DRAFT,
                new BigDecimal("100.00"), OffsetDateTime.now(), 0L);
        when(bookingOrderGroupRepository.findById(400L)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> service.getBookingGroup(400L, PATRON_ID))
                .isInstanceOf(BookingException.class);
    }

    private BookingOrder groupChild(Long id, Long groupId, BookingStatus status) {
        return BookingOrder.restore(id, PATRON_ID, 501L,
                new TimeRange(OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(1)),
                BigDecimal.ONE, new BigDecimal("100.00"), status, OffsetDateTime.now(), 0L, groupId);
    }
}

package com.mystikos.booking.domain.event;

import com.mystikos.common.event.DomainEvent;

public class BookingCreatedEvent extends DomainEvent {

    private final Long bookingId;
    private final Long patronId;
    private final Long companionId;

    public BookingCreatedEvent(Long bookingId, Long patronId, Long companionId) {
        this.bookingId = bookingId;
        this.patronId = patronId;
        this.companionId = companionId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getPatronId() {
        return patronId;
    }

    public Long getCompanionId() {
        return companionId;
    }
}

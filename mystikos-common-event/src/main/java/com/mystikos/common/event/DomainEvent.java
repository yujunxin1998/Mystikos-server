package com.mystikos.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 领域事件基类。各限界上下文的事件（如 BookingCreatedEvent）继承此类，
 * 通过 {@link DomainEventPublisher} 发布；下游上下文只依赖事件契约，
 * 不感知发布方内部实现（见 docs/architecture/domain-model.md 的上下文映射）。
 */
public abstract class DomainEvent {

    private final String eventId = UUID.randomUUID().toString();
    private final Instant occurredAt = Instant.now();

    public String getEventId() {
        return eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}

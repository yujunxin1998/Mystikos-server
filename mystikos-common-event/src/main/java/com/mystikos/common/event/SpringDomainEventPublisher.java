package com.mystikos.common.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * MVP 阶段的默认实现：直接转发给 Spring 的 {@link ApplicationEventPublisher}，
 * 事件监听者用 {@code @EventListener} 或 {@code @TransactionalEventListener} 消费，
 * 全部在同一个 JVM 进程内、同步或事务后置触发，不需要额外中间件。
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}

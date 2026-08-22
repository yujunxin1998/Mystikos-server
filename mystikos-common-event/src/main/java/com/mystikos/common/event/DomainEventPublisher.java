package com.mystikos.common.event;

/**
 * 领域事件发布端口。application 层只依赖这个接口，不感知底层是进程内事件
 * 还是消息队列——这是能在不改业务代码的前提下把事件总线换成 MQ 的关键。
 */
public interface DomainEventPublisher {

    void publish(DomainEvent event);
}

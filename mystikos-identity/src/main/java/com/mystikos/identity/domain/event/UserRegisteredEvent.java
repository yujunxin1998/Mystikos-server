package com.mystikos.identity.domain.event;

import com.mystikos.common.event.DomainEvent;

public class UserRegisteredEvent extends DomainEvent {

    private final Long userId;
    private final String username;

    public UserRegisteredEvent(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}

package com.mystikos.identity.application.command;

import com.mystikos.identity.domain.model.AuthChannel;
import com.mystikos.identity.domain.model.Role;

public record RegisterCommand(
        AuthChannel channel,
        String identifier,
        String verificationCode,
        String rawPassword,
        Role initialRole
) {
}

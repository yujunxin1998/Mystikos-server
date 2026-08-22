package com.mystikos.identity.application.command;

import com.mystikos.identity.domain.model.AuthChannel;

public record LoginCommand(
        AuthChannel channel,
        String identifier,
        CredentialType credentialType,
        String credential
) {
}

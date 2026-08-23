package com.mystikos.identity.application.service;

public record CompanionApplicationReadinessView(
        boolean oauthBound,
        String email,
        String phone,
        boolean emailVerified,
        boolean phoneVerified,
        boolean companionApplicationAllowed
) {
}

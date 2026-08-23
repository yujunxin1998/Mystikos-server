package com.mystikos.identity.application.service;

public record TagView(
        Long id,
        String category,
        String label,
        int sortOrder,
        boolean enabled
) {
}

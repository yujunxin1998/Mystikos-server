package com.mystikos.identity.application.service;

public record AuthResult(String accessToken, String refreshToken, Long userId) {
}

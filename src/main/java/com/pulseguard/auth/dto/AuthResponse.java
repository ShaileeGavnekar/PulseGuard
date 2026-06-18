package com.pulseguard.auth.dto;

public record AuthResponse(String token, String tokenType, long expiresInMs) {
}

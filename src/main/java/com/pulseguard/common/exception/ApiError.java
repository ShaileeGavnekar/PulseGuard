package com.pulseguard.common.exception;

import java.time.Instant;
import java.util.Map;

/**
 * Consistent JSON error body returned for every handled exception.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, null);
    }

    public static ApiError validation(int status, String error, String message, String path,
                                       Map<String, String> fieldErrors) {
        return new ApiError(Instant.now(), status, error, message, path, fieldErrors);
    }
}

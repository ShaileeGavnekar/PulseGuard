package com.pulseguard.common.exception;

/** Thrown when a request conflicts with existing state (e.g. duplicate email). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}

package com.pulseguard.common.exception;

/** Thrown when a requested resource does not exist (or isn't owned by the caller). */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}

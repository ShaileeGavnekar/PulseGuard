package com.pulseguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * PulseGuard — a lightweight API & website uptime monitor.
 *
 * <p>Users register endpoints to watch; a scheduled background job pings each one,
 * records latency and up/down status, and exposes the history through a secured REST API.
 */
@SpringBootApplication
@EnableScheduling
public class PulseGuardApplication {

    public static void main(String[] args) {
        SpringApplication.run(PulseGuardApplication.class, args);
    }
}

package com.pulseguard.monitor.dto;

import com.pulseguard.monitor.Monitor;
import com.pulseguard.monitor.MonitorStatus;

import java.time.Instant;

/**
 * What we expose to API clients — deliberately NOT the entity (no owner, no internals).
 */
public record MonitorResponse(
        Long id,
        String name,
        String url,
        boolean enabled,
        MonitorStatus lastStatus,
        Instant lastCheckedAt,
        Instant createdAt) {

    public static MonitorResponse from(Monitor m) {
        return new MonitorResponse(
                m.getId(), m.getName(), m.getUrl(), m.isEnabled(),
                m.getLastStatus(), m.getLastCheckedAt(), m.getCreatedAt());
    }
}

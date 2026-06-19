package com.pulseguard.monitor.dto;

/** Simple uptime rollup for a monitor. */
public record MonitorStats(
        long totalChecks,
        long upChecks,
        long downChecks,
        double uptimePercentage) {
}

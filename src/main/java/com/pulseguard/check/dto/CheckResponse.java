package com.pulseguard.check.dto;

import com.pulseguard.check.CheckResult;
import com.pulseguard.monitor.MonitorStatus;

import java.time.Instant;

public record CheckResponse(
        Long id,
        Instant checkedAt,
        MonitorStatus status,
        Integer httpStatus,
        long responseTimeMs,
        String error) {

    public static CheckResponse from(CheckResult c) {
        return new CheckResponse(
                c.getId(), c.getCheckedAt(), c.getStatus(),
                c.getHttpStatus(), c.getResponseTimeMs(), c.getError());
    }
}

package com.pulseguard.monitor.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** All fields optional; only non-null values are applied (partial update). */
public record UpdateMonitorRequest(
        @Size(max = 120) String name,
        @Pattern(regexp = "^https?://.+", message = "URL must start with http:// or https://")
        @Size(max = 2048) String url,
        Boolean enabled) {
}

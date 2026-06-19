package com.pulseguard.monitor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateMonitorRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank
        @Pattern(regexp = "^https?://.+", message = "URL must start with http:// or https://")
        @Size(max = 2048) String url,
        Boolean enabled) {
}

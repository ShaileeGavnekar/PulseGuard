package com.pulseguard.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Documents the API and wires a "bearerAuth" scheme so the Swagger UI shows an
 * Authorize button for pasting a JWT.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "PulseGuard API",
        version = "0.1.0",
        description = "Register endpoints and track their uptime, latency and status history."))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}

package com.pulseguard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Shared infrastructure beans. The {@link RestClient} here is what the health-check
 * scheduler uses to ping monitored URLs, with explicit connect/read timeouts so a slow
 * target can never stall the scheduler.
 */
@Configuration
public class AppConfig {

    @Bean
    public RestClient healthCheckRestClient(
            @Value("${pulseguard.scheduler.request-timeout-ms}") long timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}

package com.pulseguard.alert;

import com.pulseguard.monitor.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Sends alerts when a monitor changes state. If a webhook URL is configured it POSTs a
 * JSON payload (compatible with Slack/Discord incoming webhooks or any generic endpoint);
 * otherwise it simply logs the alert.
 *
 * <p>Failures to deliver an alert are caught and logged — a broken webhook must never
 * disrupt the health-check sweep.
 */
@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final RestClient restClient;
    private final String webhookUrl;

    public AlertService(RestClient healthCheckRestClient,
                        @Value("${pulseguard.alerts.webhook-url:}") String webhookUrl) {
        this.restClient = healthCheckRestClient;
        this.webhookUrl = webhookUrl;
    }

    public void monitorDown(Monitor monitor, Integer httpStatus, String error) {
        String detail = error != null ? error : (httpStatus != null ? "HTTP " + httpStatus : "unreachable");
        String message = "🔴 DOWN: \"" + monitor.getName() + "\" (" + monitor.getUrl() + ") — " + detail;
        dispatch(message, monitor, "DOWN");
    }

    public void monitorRecovered(Monitor monitor) {
        String message = "🟢 RECOVERED: \"" + monitor.getName() + "\" (" + monitor.getUrl() + ") is back UP";
        dispatch(message, monitor, "UP");
    }

    private void dispatch(String message, Monitor monitor, String status) {
        log.warn("[ALERT] {}", message);

        if (!StringUtils.hasText(webhookUrl)) {
            return; // no webhook configured — logging only
        }

        try {
            // "text" satisfies Slack; "content" satisfies Discord; the rest is structured detail.
            Map<String, Object> payload = Map.of(
                    "text", message,
                    "content", message,
                    "monitor", monitor.getName(),
                    "url", monitor.getUrl(),
                    "status", status);

            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to deliver alert webhook: {}", e.getMessage());
        }
    }
}

package com.pulseguard.check;

import com.pulseguard.alert.AlertService;
import com.pulseguard.monitor.Monitor;
import com.pulseguard.monitor.MonitorRepository;
import com.pulseguard.monitor.MonitorStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

/**
 * The heart of PulseGuard: on a fixed interval, ping every enabled monitor, record the
 * outcome as a {@link CheckResult}, and update the monitor's current status.
 *
 * <p>Each check is isolated in its own try/catch so one unreachable target never breaks
 * the whole sweep. Timeouts are enforced by the {@link RestClient} request factory.
 */
@Component
public class HealthCheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckScheduler.class);

    private final MonitorRepository monitorRepository;
    private final CheckResultRepository checkResultRepository;
    private final RestClient restClient;
    private final AlertService alertService;
    private final boolean notifyOnRecovery;

    public HealthCheckScheduler(MonitorRepository monitorRepository,
                                CheckResultRepository checkResultRepository,
                                RestClient healthCheckRestClient,
                                AlertService alertService,
                                @Value("${pulseguard.alerts.notify-on-recovery:true}") boolean notifyOnRecovery) {
        this.monitorRepository = monitorRepository;
        this.checkResultRepository = checkResultRepository;
        this.restClient = healthCheckRestClient;
        this.alertService = alertService;
        this.notifyOnRecovery = notifyOnRecovery;
    }

    @Scheduled(fixedDelayString = "${pulseguard.scheduler.interval-ms}")
    @Transactional
    public void runChecks() {
        List<Monitor> monitors = monitorRepository.findByEnabledTrue();
        if (monitors.isEmpty()) {
            return;
        }
        log.info("Running health checks for {} monitor(s)", monitors.size());
        for (Monitor monitor : monitors) {
            check(monitor);
        }
    }

    private void check(Monitor monitor) {
        MonitorStatus previousStatus = monitor.getLastStatus();
        long start = System.nanoTime();
        Integer httpStatus = null;
        MonitorStatus status;
        String error = null;
        try {
            var response = restClient.get()
                    .uri(monitor.getUrl())
                    .retrieve()
                    .toBodilessEntity();
            httpStatus = response.getStatusCode().value();
            // Treat 2xx and 3xx (redirects) as reachable/UP; only 4xx/5xx are DOWN.
            boolean reachable = response.getStatusCode().is2xxSuccessful()
                    || response.getStatusCode().is3xxRedirection();
            status = reachable ? MonitorStatus.UP : MonitorStatus.DOWN;
        } catch (org.springframework.web.client.RestClientResponseException e) {
            httpStatus = e.getStatusCode().value();
            status = MonitorStatus.DOWN;
            error = "HTTP " + httpStatus;
        } catch (Exception e) {
            status = MonitorStatus.DOWN;
            error = truncate(e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        Instant now = Instant.now();

        checkResultRepository.save(CheckResult.builder()
                .monitor(monitor)
                .checkedAt(now)
                .status(status)
                .httpStatus(httpStatus)
                .responseTimeMs(elapsedMs)
                .error(error)
                .build());

        monitor.setLastStatus(status);
        monitor.setLastCheckedAt(now);
        monitorRepository.save(monitor);

        // Alert only on a state transition, so we don't notify every cycle.
        if (status == MonitorStatus.DOWN && previousStatus != MonitorStatus.DOWN) {
            alertService.monitorDown(monitor, httpStatus, error);
        } else if (status == MonitorStatus.UP && previousStatus == MonitorStatus.DOWN && notifyOnRecovery) {
            alertService.monitorRecovered(monitor);
        }
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() > 500 ? s.substring(0, 500) : s;
    }
}

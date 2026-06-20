package com.pulseguard.check;

import com.pulseguard.monitor.Monitor;
import com.pulseguard.monitor.MonitorStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * The outcome of a single ping against a {@link Monitor}: status, HTTP code, latency.
 */
@Entity
@Table(name = "check_results",
        indexes = @Index(name = "idx_check_monitor_time", columnList = "monitor_id, checkedAt"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitor_id", nullable = false)
    private Monitor monitor;

    @Column(nullable = false)
    private Instant checkedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MonitorStatus status;

    private Integer httpStatus;

    private long responseTimeMs;

    @Column(length = 500)
    private String error;
}

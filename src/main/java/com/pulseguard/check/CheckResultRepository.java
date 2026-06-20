package com.pulseguard.check;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {

    Page<CheckResult> findByMonitorIdOrderByCheckedAtDesc(Long monitorId, Pageable pageable);

    long countByMonitorIdAndStatus(Long monitorId, com.pulseguard.monitor.MonitorStatus status);

    long countByMonitorId(Long monitorId);
}

package com.pulseguard.monitor;

import com.pulseguard.check.CheckResultRepository;
import com.pulseguard.common.SecurityUtils;
import com.pulseguard.common.exception.NotFoundException;
import com.pulseguard.monitor.dto.CreateMonitorRequest;
import com.pulseguard.monitor.dto.MonitorResponse;
import com.pulseguard.monitor.dto.MonitorStats;
import com.pulseguard.monitor.dto.UpdateMonitorRequest;
import com.pulseguard.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for monitors. Every read/write is scoped to the authenticated owner,
 * so one user can never see or touch another user's monitors.
 */
@Service
public class MonitorService {

    private final MonitorRepository monitorRepository;
    private final CheckResultRepository checkResultRepository;
    private final SecurityUtils securityUtils;

    public MonitorService(MonitorRepository monitorRepository,
                          CheckResultRepository checkResultRepository,
                          SecurityUtils securityUtils) {
        this.monitorRepository = monitorRepository;
        this.checkResultRepository = checkResultRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public MonitorResponse create(CreateMonitorRequest request) {
        User owner = securityUtils.currentUser();
        Monitor monitor = Monitor.builder()
                .owner(owner)
                .name(request.name())
                .url(request.url())
                .enabled(request.enabled() == null || request.enabled())
                .lastStatus(MonitorStatus.UNKNOWN)
                .build();
        return MonitorResponse.from(monitorRepository.save(monitor));
    }

    @Transactional(readOnly = true)
    public Page<MonitorResponse> listMine(Pageable pageable) {
        Long ownerId = securityUtils.currentUser().getId();
        return monitorRepository.findByOwnerId(ownerId, pageable).map(MonitorResponse::from);
    }

    @Transactional(readOnly = true)
    public MonitorResponse get(Long id) {
        return MonitorResponse.from(requireOwned(id));
    }

    @Transactional
    public MonitorResponse update(Long id, UpdateMonitorRequest request) {
        Monitor monitor = requireOwned(id);
        if (request.name() != null) monitor.setName(request.name());
        if (request.url() != null) monitor.setUrl(request.url());
        if (request.enabled() != null) monitor.setEnabled(request.enabled());
        return MonitorResponse.from(monitorRepository.save(monitor));
    }

    @Transactional
    public void delete(Long id) {
        monitorRepository.delete(requireOwned(id));
    }

    @Transactional(readOnly = true)
    public MonitorStats stats(Long id) {
        Monitor monitor = requireOwned(id);
        long total = checkResultRepository.countByMonitorId(monitor.getId());
        long up = checkResultRepository.countByMonitorIdAndStatus(monitor.getId(), MonitorStatus.UP);
        long down = total - up;
        double uptime = total == 0 ? 0.0 : Math.round((up * 10000.0) / total) / 100.0;
        return new MonitorStats(total, up, down, uptime);
    }

    /** Loads a monitor and verifies the current user owns it, else 404. */
    private Monitor requireOwned(Long id) {
        Long ownerId = securityUtils.currentUser().getId();
        return monitorRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new NotFoundException("Monitor not found: " + id));
    }
}

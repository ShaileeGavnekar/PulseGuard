package com.pulseguard.monitor;

import com.pulseguard.check.CheckResultRepository;
import com.pulseguard.check.dto.CheckResponse;
import com.pulseguard.monitor.dto.CreateMonitorRequest;
import com.pulseguard.monitor.dto.MonitorResponse;
import com.pulseguard.monitor.dto.MonitorStats;
import com.pulseguard.monitor.dto.UpdateMonitorRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CRUD for the authenticated user's monitors, plus uptime stats and check history.
 */
@RestController
@RequestMapping("/api/monitors")
@Tag(name = "Monitors", description = "Manage monitored endpoints")
@SecurityRequirement(name = "bearerAuth")
public class MonitorController {

    private final MonitorService monitorService;
    private final CheckResultRepository checkResultRepository;

    public MonitorController(MonitorService monitorService, CheckResultRepository checkResultRepository) {
        this.monitorService = monitorService;
        this.checkResultRepository = checkResultRepository;
    }

    @Operation(summary = "Create a monitor")
    @PostMapping
    public ResponseEntity<MonitorResponse> create(@Valid @RequestBody CreateMonitorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(monitorService.create(request));
    }

    @Operation(summary = "List my monitors (paged)")
    @GetMapping
    public Page<MonitorResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        return monitorService.listMine(pageable);
    }

    @Operation(summary = "Get one monitor")
    @GetMapping("/{id}")
    public MonitorResponse get(@PathVariable Long id) {
        return monitorService.get(id);
    }

    @Operation(summary = "Update a monitor (partial)")
    @PatchMapping("/{id}")
    public MonitorResponse update(@PathVariable Long id, @Valid @RequestBody UpdateMonitorRequest request) {
        return monitorService.update(id, request);
    }

    @Operation(summary = "Delete a monitor")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        monitorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Uptime statistics for a monitor")
    @GetMapping("/{id}/stats")
    public MonitorStats stats(@PathVariable Long id) {
        return monitorService.stats(id);
    }

    @Operation(summary = "Recent check history for a monitor (paged, newest first)")
    @GetMapping("/{id}/checks")
    public Page<CheckResponse> checks(@PathVariable Long id,
                                      @PageableDefault(size = 50) Pageable pageable) {
        // Ownership is enforced by fetching stats first (throws 404 if not owned).
        monitorService.get(id);
        return checkResultRepository
                .findByMonitorIdOrderByCheckedAtDesc(id, pageable)
                .map(CheckResponse::from);
    }
}

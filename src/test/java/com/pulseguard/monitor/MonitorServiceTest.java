package com.pulseguard.monitor;

import com.pulseguard.check.CheckResultRepository;
import com.pulseguard.common.SecurityUtils;
import com.pulseguard.common.exception.NotFoundException;
import com.pulseguard.monitor.dto.CreateMonitorRequest;
import com.pulseguard.monitor.dto.MonitorResponse;
import com.pulseguard.monitor.dto.MonitorStats;
import com.pulseguard.user.Role;
import com.pulseguard.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link MonitorService} — no Spring context, just Mockito.
 */
@ExtendWith(MockitoExtension.class)
class MonitorServiceTest {

    @Mock MonitorRepository monitorRepository;
    @Mock CheckResultRepository checkResultRepository;
    @Mock SecurityUtils securityUtils;

    @InjectMocks MonitorService monitorService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).email("a@b.com").displayName("A").role(Role.USER).build();
    }

    @Test
    void create_savesMonitorOwnedByCurrentUser_andDefaultsEnabledTrue() {
        when(securityUtils.currentUser()).thenReturn(owner);
        when(monitorRepository.save(any(Monitor.class))).thenAnswer(inv -> {
            Monitor m = inv.getArgument(0);
            m.setId(99L);
            return m;
        });

        MonitorResponse res = monitorService.create(
                new CreateMonitorRequest("My API", "https://example.com/health", null));

        assertThat(res.id()).isEqualTo(99L);
        assertThat(res.enabled()).isTrue();
        assertThat(res.lastStatus()).isEqualTo(MonitorStatus.UNKNOWN);
        assertThat(res.url()).isEqualTo("https://example.com/health");
    }

    @Test
    void stats_computesUptimePercentage() {
        when(securityUtils.currentUser()).thenReturn(owner);
        Monitor monitor = Monitor.builder().id(5L).owner(owner).name("x").url("https://x").enabled(true).build();
        when(monitorRepository.findByIdAndOwnerId(5L, 1L)).thenReturn(Optional.of(monitor));
        when(checkResultRepository.countByMonitorId(5L)).thenReturn(10L);
        when(checkResultRepository.countByMonitorIdAndStatus(5L, MonitorStatus.UP)).thenReturn(9L);

        MonitorStats stats = monitorService.stats(5L);

        assertThat(stats.totalChecks()).isEqualTo(10L);
        assertThat(stats.upChecks()).isEqualTo(9L);
        assertThat(stats.downChecks()).isEqualTo(1L);
        assertThat(stats.uptimePercentage()).isEqualTo(90.0);
    }

    @Test
    void get_otherUsersMonitor_throwsNotFound() {
        when(securityUtils.currentUser()).thenReturn(owner);
        when(monitorRepository.findByIdAndOwnerId(eq(42L), eq(1L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> monitorService.get(42L))
                .isInstanceOf(NotFoundException.class);
    }
}

package com.pulseguard.config;

import com.pulseguard.monitor.Monitor;
import com.pulseguard.monitor.MonitorRepository;
import com.pulseguard.monitor.MonitorStatus;
import com.pulseguard.user.Role;
import com.pulseguard.user.User;
import com.pulseguard.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * Seeds a demo user and a few real monitors on startup so the app has data to show
 * immediately (handy for demos). Skips seeding if the demo user already exists.
 *
 * <p>Demo credentials:  email = demo@pulseguard.dev  ·  password = password123
 */
@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEMO_EMAIL = "demo@pulseguard.dev";
    private static final String DEMO_PASSWORD = "password123";

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               MonitorRepository monitorRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.existsByEmail(DEMO_EMAIL)) {
                return; // already seeded
            }

            User demo = userRepository.save(User.builder()
                    .email(DEMO_EMAIL)
                    .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                    .displayName("Demo User")
                    .role(Role.USER)
                    .build());

            List<String[]> seeds = List.of(
                    new String[]{"Flaky Service", "https://httpstat.us/random/200,503"},
                    new String[]{"Google", "https://www.google.com"},
                    new String[]{"GitHub", "https://github.com"},
                    new String[]{"Example.com", "https://example.com"},
                    new String[]{"Wikipedia", "https://www.wikipedia.org"}
            );

            for (String[] s : seeds) {
                monitorRepository.save(Monitor.builder()
                        .owner(demo)
                        .name(s[0])
                        .url(s[1])
                        .enabled(true)
                        .lastStatus(MonitorStatus.UNKNOWN)
                        .build());
            }

            log.info("Seeded demo user ({}) with {} monitors. Login with password '{}'.",
                    DEMO_EMAIL, seeds.size(), DEMO_PASSWORD);
        };
    }
}

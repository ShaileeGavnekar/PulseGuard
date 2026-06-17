package com.pulseguard.config;

import com.pulseguard.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT security configuration using the Spring Security 6 component-based DSL.
 *
 * <ul>
 *   <li>{@code /api/auth/**}, Swagger UI and actuator health are public.</li>
 *   <li>Everything else requires a valid Bearer token.</li>
 *   <li>Sessions are STATELESS and CSRF is disabled — correct for a token-only API.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize on service/controller methods
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/",
                        "/dashboard.html",
                        "/favicon.ico",
                        "/css/**",
                        "/js/**",
                        "/api/auth/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/actuator/health",
                        "/h2-console/**").permitAll()
                .anyRequest().authenticated())
            // Return 401 instead of redirecting to a login page for unauthenticated API calls
            .exceptionHandling(ex -> ex.authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            // Allow the H2 console to render in a frame during local dev
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the AuthenticationManager. Spring Security automatically builds a
     * DaoAuthenticationProvider from the {@code CustomUserDetailsService} and
     * {@code PasswordEncoder} beans in the context, so no manual provider wiring is needed.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

package com.pulseguard.auth;

import com.pulseguard.auth.dto.AuthResponse;
import com.pulseguard.auth.dto.LoginRequest;
import com.pulseguard.auth.dto.RegisterRequest;
import com.pulseguard.common.exception.ConflictException;
import com.pulseguard.security.JwtService;
import com.pulseguard.user.Role;
import com.pulseguard.user.User;
import com.pulseguard.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final long expirationMs;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       @Value("${pulseguard.security.jwt-expiration-ms}") long expirationMs) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.expirationMs = expirationMs;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("An account with that email already exists");
        }
        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .role(Role.USER)
                .build();
        userRepository.save(user);
        return buildResponse(jwtService.generateToken(user));
    }

    public AuthResponse login(LoginRequest request) {
        // Throws on bad credentials -> handled by the global exception handler as 401.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email()).orElseThrow();
        return buildResponse(jwtService.generateToken(user));
    }

    private AuthResponse buildResponse(String token) {
        return new AuthResponse(token, "Bearer", expirationMs);
    }
}

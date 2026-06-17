package com.pulseguard.security;

import com.pulseguard.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

/**
 * Issues and validates signed JWTs (HMAC-SHA256).
 *
 * <p>The token's subject is the user's email; a custom "role" claim carries the authority.
 * This is intentionally small and library-light so the mechanics are easy to follow.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${pulseguard.security.jwt-secret}") String base64Secret,
            @Value("${pulseguard.security.jwt-expiration-ms}") long expirationMs) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .claim("name", user.getDisplayName())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isValid(String token, String expectedEmail) {
        try {
            final String email = extractEmail(token);
            return email.equals(expectedEmail) && !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}

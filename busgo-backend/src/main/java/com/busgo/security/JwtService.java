package com.busgo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Creates and validates signed JWTs (HS256).
 *
 * Token contents: sub = user id, jti = unique token id (used for logout), plus
 * "email" and "isAdmin" claims for convenience. The server never trusts the isAdmin claim:
 * authorities are re-loaded from the database on every request.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final Duration expiration;

    public JwtService(@Value("${busgo.jwt.secret}") String secret,
                      @Value("${busgo.jwt.expiration-minutes}") long expirationMinutes) {
        // hmacShaKeyFor throws WeakKeyException if the secret is shorter than 256 bits (32 bytes).
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    public String generateToken(AppUserDetails user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .claim("email", user.getUsername())
                .claim("isAdmin", user.isAdmin())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(key)
                .compact();
    }

    /**
     * Verifies the signature and expiry and returns the claims.
     *
     * @throws io.jsonwebtoken.JwtException if the token is malformed, tampered with or expired
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

package com.busgo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-1234567890";

    private final JwtService jwtService = new JwtService(SECRET, 60);
    private final UUID userId = UUID.randomUUID();
    private final AppUserDetails user =
            new AppUserDetails(userId, "jane@example.com", "hash", "Jane", false);

    @Test
    void generatedToken_canBeParsed_andContainsUserClaims() {
        String token = jwtService.generateToken(user);

        Claims claims = jwtService.parse(token);

        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("jane@example.com", claims.get("email", String.class));
        assertFalse(claims.get("isAdmin", Boolean.class));
        assertNotNull(claims.getId());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void everyToken_hasItsOwnId() {
        Claims first = jwtService.parse(jwtService.generateToken(user));
        Claims second = jwtService.parse(jwtService.generateToken(user));

        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    void adminFlag_isCarriedInTheToken() {
        AppUserDetails admin = new AppUserDetails(UUID.randomUUID(), "admin@busgo.com", "hash", "Admin", true);

        Claims claims = jwtService.parse(jwtService.generateToken(admin));

        assertTrue(claims.get("isAdmin", Boolean.class));
    }

    @Test
    void tokenSignedWithAnotherKey_isRejected() {
        JwtService other = new JwtService("another-secret-another-secret-1234567890!!", 60);
        String foreignToken = other.generateToken(user);

        assertThrows(JwtException.class, () -> jwtService.parse(foreignToken));
    }

    @Test
    void tamperedToken_isRejected() {
        String token = jwtService.generateToken(user);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThrows(JwtException.class, () -> jwtService.parse(tampered));
    }

    @Test
    void expiredToken_isRejected() {
        JwtService alreadyExpired = new JwtService(SECRET, -1);
        String token = alreadyExpired.generateToken(user);

        assertThrows(ExpiredJwtException.class, () -> jwtService.parse(token));
    }
}

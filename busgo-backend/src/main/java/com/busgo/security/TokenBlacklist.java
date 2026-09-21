package com.busgo.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory list of logged-out token ids (jti), kept until the token would have expired anyway.
 * Good enough for a single-instance Foundation app; with several instances or restarts
 * you would move this to Redis/DB (documented trade-off).
 */
@Component
public class TokenBlacklist {

    private final Map<String, Instant> revoked = new ConcurrentHashMap<>();

    public void revoke(String tokenId, Instant expiresAt) {
        purgeExpired();
        revoked.put(tokenId, expiresAt);
    }

    public boolean isRevoked(String tokenId) {
        return tokenId != null && revoked.containsKey(tokenId);
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        revoked.values().removeIf(expiresAt -> expiresAt.isBefore(now));
    }
}

package com.busgo.security;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBlacklistTest {

    private final TokenBlacklist blacklist = new TokenBlacklist();

    @Test
    void revokedToken_isReportedAsRevoked() {
        blacklist.revoke("jti-1", Instant.now().plus(1, ChronoUnit.HOURS));

        assertTrue(blacklist.isRevoked("jti-1"));
        assertFalse(blacklist.isRevoked("jti-2"));
    }

    @Test
    void nullTokenId_isNeverRevoked() {
        assertFalse(blacklist.isRevoked(null));
    }

    @Test
    void expiredEntries_areRemovedOnNextRevoke() {
        blacklist.revoke("old", Instant.now().minus(1, ChronoUnit.MINUTES));
        blacklist.revoke("new", Instant.now().plus(1, ChronoUnit.HOURS));

        assertFalse(blacklist.isRevoked("old"));
        assertTrue(blacklist.isRevoked("new"));
    }
}

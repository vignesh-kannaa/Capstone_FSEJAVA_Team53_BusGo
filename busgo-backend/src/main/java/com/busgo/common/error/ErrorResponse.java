package com.busgo.common.error;

import java.time.Instant;

/**
 * The single error format used by every endpoint (agreed contract):
 * { "timestamp": "...", "path": "/api/...", "error": "VALIDATION_ERROR", "message": "..." }
 */
public record ErrorResponse(Instant timestamp, String path, String error, String message) {

    public static ErrorResponse of(String path, String error, String message) {
        return new ErrorResponse(Instant.now(), path, error, message);
    }
}

package com.busgo.auth.dto;

/** Login result: the JWT plus the user, so the UI can fill its auth context without a second call. */
public record AuthResponse(String token, UserResponse user) {
}

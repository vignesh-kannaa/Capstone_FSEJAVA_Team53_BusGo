package com.busgo.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "is required")
        @Email(message = "must be a valid email address")
        @Size(max = 254, message = "must be at most 254 characters")
        String email,

        // BCrypt only uses the first 72 bytes, so longer passwords are rejected instead of silently truncated.
        @NotBlank(message = "is required")
        @Size(min = 8, max = 72, message = "must be between 8 and 72 characters")
        String password,

        @NotBlank(message = "is required")
        @Size(max = 100, message = "must be at most 100 characters")
        String name) {
}

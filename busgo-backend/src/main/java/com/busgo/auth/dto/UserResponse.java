package com.busgo.auth.dto;

import com.busgo.security.AppUserDetails;
import com.busgo.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/** Public view of a user. Never contains the password hash. JSON field is named "isAdmin". */
public record UserResponse(UUID id, String email, String name, @JsonProperty("isAdmin") boolean admin) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.isAdmin());
    }

    public static UserResponse from(AppUserDetails user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getName(), user.isAdmin());
    }
}

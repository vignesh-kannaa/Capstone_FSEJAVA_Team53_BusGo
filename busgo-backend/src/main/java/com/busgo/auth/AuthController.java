package com.busgo.auth;

import com.busgo.auth.dto.AuthResponse;
import com.busgo.auth.dto.LoginRequest;
import com.busgo.auth.dto.RegisterRequest;
import com.busgo.auth.dto.UserResponse;
import com.busgo.security.AppUserDetails;
import com.busgo.security.JwtAuthenticationFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new passenger account")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Log in and receive a JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Log out (invalidates the current token)")
    public void logout(HttpServletRequest request) {
        authService.logout(JwtAuthenticationFilter.extractToken(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Current user (used by the UI to restore a session after a page refresh)")
    public UserResponse me(@AuthenticationPrincipal AppUserDetails currentUser) {
        return UserResponse.from(currentUser);
    }
}

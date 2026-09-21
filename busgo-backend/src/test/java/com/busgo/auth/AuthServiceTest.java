package com.busgo.auth;

import com.busgo.auth.dto.AuthResponse;
import com.busgo.auth.dto.LoginRequest;
import com.busgo.auth.dto.RegisterRequest;
import com.busgo.auth.dto.UserResponse;
import com.busgo.common.error.ApiException;
import com.busgo.security.AppUserDetails;
import com.busgo.security.JwtService;
import com.busgo.security.TokenBlacklist;
import com.busgo.user.User;
import com.busgo.user.UserRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private TokenBlacklist tokenBlacklist;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_savesNormalUserWithHashedPasswordAndLowercaseEmail() {
        when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Passw0rd!")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authService.register(new RegisterRequest("John@Example.com", "Passw0rd!", " John "));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertEquals("john@example.com", saved.getValue().getEmail());
        assertEquals("John", saved.getValue().getName());
        assertEquals("hashed-password", saved.getValue().getPasswordHash());
        assertFalse(saved.getValue().isAdmin());
        assertEquals("john@example.com", response.email());
        assertFalse(response.admin());
    }

    @Test
    void register_withExistingEmail_throwsConflict() {
        when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class,
                () -> authService.register(new RegisterRequest("john@example.com", "Passw0rd!", "John")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("EMAIL_ALREADY_EXISTS", ex.getCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_withValidCredentials_returnsTokenAndUser() {
        AppUserDetails principal = new AppUserDetails(UUID.randomUUID(), "john@example.com", "hash", "John", true);
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        when(jwtService.generateToken(principal)).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest(" John@Example.com ", "Passw0rd!"));

        assertEquals("jwt-token", response.token());
        assertEquals("john@example.com", response.user().email());
        assertTrue(response.user().admin());
    }

    @Test
    void login_withBadCredentials_propagatesBadCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("john@example.com", "wrong-password")));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void logout_revokesTheTokenUntilItExpires() {
        Date expiry = new Date(System.currentTimeMillis() + 60_000);
        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("token-id-1");
        when(claims.getExpiration()).thenReturn(expiry);
        when(jwtService.parse("some-token")).thenReturn(claims);

        authService.logout("some-token");

        verify(tokenBlacklist).revoke("token-id-1", expiry.toInstant());
    }

    @Test
    void logout_withoutToken_throwsUnauthorized() {
        ApiException ex = assertThrows(ApiException.class, () -> authService.logout(null));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }
}

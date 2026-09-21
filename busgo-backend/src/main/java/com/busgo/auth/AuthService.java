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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklist tokenBlacklist;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       TokenBlacklist tokenBlacklist) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenBlacklist = tokenBlacklist;
    }

    /** Creates a normal (non-admin) user. Emails are stored lower-cased and are unique. */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("EMAIL_ALREADY_EXISTS", "An account with this email already exists");
        }
        User user = new User();
        user.setEmail(email);
        user.setName(request.name().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setAdmin(false); // admins can only be created by seeding, never through the public API
        return UserResponse.from(userRepository.save(user));
    }

    /**
     * Verifies the credentials (BCrypt) and issues a JWT.
     *
     * @throws org.springframework.security.authentication.BadCredentialsException on wrong email or password
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizeEmail(request.email()), request.password()));
        AppUserDetails principal = (AppUserDetails) authentication.getPrincipal();
        return new AuthResponse(jwtService.generateToken(principal), UserResponse.from(principal));
    }

    /** Stateless JWT cannot be "deleted", so the token id is blacklisted until the token would expire. */
    public void logout(String token) {
        if (token == null) {
            throw ApiException.unauthorized("Authentication is required to log out");
        }
        Claims claims = jwtService.parse(token);
        tokenBlacklist.revoke(claims.getId(), claims.getExpiration().toInstant());
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

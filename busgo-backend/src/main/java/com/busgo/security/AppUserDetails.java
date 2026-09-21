package com.busgo.security;

import com.busgo.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * The authenticated principal. Controllers of other features can receive it with
 * {@code @AuthenticationPrincipal AppUserDetails currentUser} to get the logged-in user's id.
 *
 * Authorities: every user has ROLE_USER; users with {@code isAdmin = true} also get ROLE_ADMIN,
 * so admin endpoints can use {@code @PreAuthorize("hasRole('ADMIN')")}.
 */
public class AppUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final String name;
    private final boolean admin;

    public AppUserDetails(UUID id, String email, String passwordHash, String name, boolean admin) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.admin = admin;
    }

    public static AppUserDetails from(User user) {
        return new AppUserDetails(user.getId(), user.getEmail(), user.getPasswordHash(),
                user.getName(), user.isAdmin());
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isAdmin() {
        return admin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return admin
                ? List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}

package com.busgo.security;

import com.busgo.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Used at login. The "username" is the user's email (case-insensitive). */
    @Override
    public AppUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmailIgnoreCase(email)
                .map(AppUserDetails::from)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /** Used on every authenticated request (token subject = user id). */
    public AppUserDetails loadUserById(UUID id) throws UsernameNotFoundException {
        return userRepository.findById(id)
                .map(AppUserDetails::from)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}

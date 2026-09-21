package com.busgo.config;

import com.busgo.user.User;
import com.busgo.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Seeds the single admin user (isAdmin = true) on startup if it does not exist yet.
 * The password is hashed at runtime with BCrypt (the sample SQL in the project doc uses a fake hash).
 * Credentials come from busgo.seed.admin.* (defaults: admin@busgo.com / Admin@123 - dev only!).
 *
 * Other developers: add your own seeders (buses, schedules) as separate ApplicationRunner classes
 * with a later @Order so that everything loads in a predictable sequence.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminName;

    public DataSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${busgo.seed.admin.email}") String adminEmail,
                      @Value("${busgo.seed.admin.password}") String adminPassword,
                      @Value("${busgo.seed.admin.name}") String adminName) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        this.adminPassword = adminPassword;
        this.adminName = adminName;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
            return;
        }
        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setName(adminName);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setAdmin(true);
        userRepository.save(admin);
        log.info("Seeded admin user {}", adminEmail);
    }
}

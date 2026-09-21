package com.busgo.config;

import com.busgo.security.JwtAuthenticationFilter;
import com.busgo.security.RestAccessDeniedHandler;
import com.busgo.security.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * Stateless JWT security.
 *
 * URL rules (central, so they are easy to review on the "Admin security" day):
 *  - public:  register, login, swagger, h2-console, GET /api/schedules/** (search + seat map)
 *  - admin:   /api/buses/** and every non-GET /api/schedules/**
 *  - all other endpoints (bookings, logout, me, ...) need a valid token
 *
 * Feature owners can add finer checks with {@code @PreAuthorize("hasRole('ADMIN')")} (method security is on).
 *
 * Ant matchers are used on purpose: with the H2 console servlet registered next to Spring MVC,
 * plain string matchers are ambiguous and Spring Security refuses to start.
 */
@Configuration
@EnableMethodSecurity
@SuppressWarnings("deprecation")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtFilter,
                                                   RestAuthenticationEntryPoint entryPoint,
                                                   RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable) // stateless API, token in header (no cookies)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(h -> h.frameOptions(f -> f.sameOrigin())) // needed by the H2 console
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(antMatcher(HttpMethod.OPTIONS, "/**")).permitAll()
                        .requestMatchers(
                                antMatcher("/error"),
                                antMatcher("/h2-console/**"),
                                antMatcher("/swagger-ui.html"),
                                antMatcher("/swagger-ui/**"),
                                antMatcher("/v3/api-docs/**")).permitAll()
                        .requestMatchers(
                                antMatcher(HttpMethod.POST, "/api/auth/register"),
                                antMatcher(HttpMethod.POST, "/api/auth/login")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api/schedules/**")).permitAll()
                        .requestMatchers(
                                antMatcher("/api/buses/**"),
                                antMatcher("/api/schedules/**")).hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /** The filter is added to the security chain above; stop Boot from also registering it as a plain servlet filter. */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${busgo.cors.allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}

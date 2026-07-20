package com.enterprise.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Resource Server security configuration.
 * Validates JWT Bearer tokens issued by the Authorization Server.
 *
 * Design Pattern: Chain of Responsibility (Spring Security filter chain).
 */
@Configuration
@EnableMethodSecurity
public class ResourceServerConfig {

    @Bean
    public SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/api/v1/auth/token",
                                "/v3/api-docs/**",
                                "/swagger-ui/**"
                        ).permitAll()
                        // Admin-only endpoints
                        .requestMatchers("/api/v1/admin/**").hasAuthority("SCOPE_write")
                        // All other API calls require a valid token
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(rs -> rs
                        .jwt(jwt -> jwt.jwkSetUri("http://localhost:9000/oauth2/jwks")));

        return http.build();
    }
}

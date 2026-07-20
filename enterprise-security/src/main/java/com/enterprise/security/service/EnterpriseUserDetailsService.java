package com.enterprise.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bridge between Spring Security and the enterprise UserRepository.
 *
 * Design Pattern: Adapter — adapts our User entity to Spring Security's UserDetails contract.
 */
@Service
@RequiredArgsConstructor
public class EnterpriseUserDetailsService implements UserDetailsService {

    // NOTE: We use a lightweight record to avoid a circular dependency between
    // enterprise-security and enterprise-repository at this stage.
    // In production, inject UserRepository directly (both modules in the same service).

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Placeholder — in the REST API module this is wired to the real UserRepository.
        throw new UsernameNotFoundException("User not found: " + username);
    }

    /**
     * Converts a list of role strings (e.g. "ROLE_ADMIN") to GrantedAuthority objects.
     */
    public static Collection<GrantedAuthority> toGrantedAuthorities(List<String> roles) {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}

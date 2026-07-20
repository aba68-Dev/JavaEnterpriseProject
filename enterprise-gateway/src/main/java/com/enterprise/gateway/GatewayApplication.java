package com.enterprise.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Enterprise Gateway — OAuth 2.1 Authorization Server.
 *
 * Responsibilities:
 *   - Issues JWT access tokens (Authorization Code + PKCE, Client Credentials)
 *   - Provides JWKS endpoint for resource servers to validate tokens
 *   - Hosts login UI and consent page
 *
 * Endpoints (Spring Authorization Server defaults):
 *   POST /oauth2/token              — issue tokens
 *   GET  /oauth2/authorize          — authorization endpoint
 *   GET  /oauth2/jwks               — public keys
 *   POST /oauth2/revoke             — token revocation
 *   POST /oauth2/introspect         — token introspection
 *   GET  /.well-known/openid-configuration — OIDC discovery
 */
@SpringBootApplication(scanBasePackages = {
        "com.enterprise.gateway",
        "com.enterprise.security"
})
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}

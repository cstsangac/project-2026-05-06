package demo.userwallet.application.security;

import java.util.UUID;

public record JwtPrincipal(UUID userId, String username) {}


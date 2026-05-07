package demo.userwallet.api.auth;

import java.util.UUID;

public final class AuthResponses {
  private AuthResponses() {}

  public record RegisterResponse(UUID userId) {}

  public record LoginResponse(String accessToken, String tokenType) {}
}


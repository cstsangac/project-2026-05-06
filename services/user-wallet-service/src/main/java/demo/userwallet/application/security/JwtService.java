package demo.userwallet.application.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final String issuer;
  private final SecretKey key;
  private final long ttlSeconds;

  public JwtService(
      @Value("${app.jwt.issuer}") String issuer,
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.ttl-seconds}") long ttlSeconds) {
    this.issuer = issuer;
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.ttlSeconds = ttlSeconds;
  }

  public String mint(UUID userId, String username) {
    Instant now = Instant.now();
    return Jwts.builder()
        .issuer(issuer)
        .subject(userId.toString())
        .claim("username", username)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(ttlSeconds)))
        .signWith(key)
        .compact();
  }

  public JwtPrincipal verify(String token) {
    Jwt<?, Claims> parsed =
        Jwts.parser()
            .verifyWith(key)
            .requireIssuer(issuer)
            .build()
            .parseSignedClaims(token);
    Claims claims = parsed.getPayload();
    UUID userId = UUID.fromString(claims.getSubject());
    String username = claims.get("username", String.class);
    return new JwtPrincipal(userId, username);
  }
}


package demo.userwallet.application;

import demo.userwallet.api.ConflictException;
import demo.userwallet.application.security.JwtService;
import demo.userwallet.domain.Money;
import demo.userwallet.infrastructure.jpa.AppUserEntity;
import demo.userwallet.infrastructure.jpa.AppUserRepository;
import demo.userwallet.infrastructure.jpa.WalletEntity;
import demo.userwallet.infrastructure.jpa.WalletRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final AppUserRepository userRepository;
  private final WalletRepository walletRepository;
  private final JwtService jwtService;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public AuthService(
      AppUserRepository userRepository, WalletRepository walletRepository, JwtService jwtService) {
    this.userRepository = userRepository;
    this.walletRepository = walletRepository;
    this.jwtService = jwtService;
  }

  @Transactional
  public UUID register(String username, String password) {
    if (userRepository.findByUsername(username).isPresent()) {
      throw new ConflictException("Username already exists");
    }
    AppUserEntity user = new AppUserEntity();
    user.setId(UUID.randomUUID());
    user.setUsername(username);
    user.setPasswordHash(passwordEncoder.encode(password));
    user.setCreatedAt(Instant.now());
    userRepository.save(user);

    WalletEntity wallet = new WalletEntity();
    wallet.setUserId(user.getId());
    wallet.setBalanceAmount(BigDecimal.ZERO.setScale(Money.SCALE));
    wallet.setUpdatedAt(Instant.now());
    walletRepository.save(wallet);

    return user.getId();
  }

  public String login(String username, String password) {
    AppUserEntity user =
        userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Bad credentials"));
    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new IllegalArgumentException("Bad credentials");
    }
    return jwtService.mint(user.getId(), user.getUsername());
  }
}


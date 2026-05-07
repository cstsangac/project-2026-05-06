package demo.userwallet.application;

import demo.userwallet.api.NotFoundException;
import demo.userwallet.domain.Money;
import demo.userwallet.infrastructure.jpa.WalletEntity;
import demo.userwallet.infrastructure.jpa.WalletRepository;
import demo.userwallet.infrastructure.jpa.WalletTxEntity;
import demo.userwallet.infrastructure.jpa.WalletTxRepository;
import demo.userwallet.infrastructure.jpa.WalletTxType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {
  private final WalletRepository walletRepository;
  private final WalletTxRepository walletTxRepository;

  public WalletService(WalletRepository walletRepository, WalletTxRepository walletTxRepository) {
    this.walletRepository = walletRepository;
    this.walletTxRepository = walletTxRepository;
  }

  public WalletEntity getWallet(UUID userId) {
    return walletRepository.findById(userId).orElseThrow(() -> new NotFoundException("Wallet not found"));
  }

  public List<WalletTxEntity> recentTx(UUID userId, int limit) {
    return walletTxRepository.findRecentByUserId(userId, PageRequest.of(0, limit));
  }

  @Transactional
  public WalletEntity topUp(UUID userId, BigDecimal amount, String referenceId) {
    amount = Money.normalize(amount);
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("amount must be > 0");
    }
    WalletEntity wallet = getWallet(userId);
    BigDecimal newBalance = Money.normalize(wallet.getBalanceAmount().add(amount));
    wallet.setBalanceAmount(newBalance);
    wallet.setUpdatedAt(Instant.now());
    walletRepository.save(wallet);

    WalletTxEntity tx = new WalletTxEntity();
    tx.setId(UUID.randomUUID());
    tx.setUserId(userId);
    tx.setTxType(WalletTxType.TOPUP);
    tx.setAmount(amount);
    tx.setBalanceAfterAmount(newBalance);
    tx.setReferenceId(referenceId);
    tx.setCreatedAt(Instant.now());
    walletTxRepository.save(tx);

    return wallet;
  }

  @Transactional
  public WalletEntity debitForGift(UUID userId, BigDecimal amount, String referenceId) {
    amount = Money.normalize(amount);
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("amount must be > 0");
    }
    WalletEntity wallet = getWallet(userId);
    if (wallet.getBalanceAmount().compareTo(amount) < 0) {
      throw new IllegalArgumentException("Insufficient balance");
    }
    BigDecimal newBalance = Money.normalize(wallet.getBalanceAmount().subtract(amount));
    wallet.setBalanceAmount(newBalance);
    wallet.setUpdatedAt(Instant.now());
    walletRepository.save(wallet);

    WalletTxEntity tx = new WalletTxEntity();
    tx.setId(UUID.randomUUID());
    tx.setUserId(userId);
    tx.setTxType(WalletTxType.GIFT_DEBIT);
    tx.setAmount(amount);
    tx.setBalanceAfterAmount(newBalance);
    tx.setReferenceId(referenceId);
    tx.setCreatedAt(Instant.now());
    walletTxRepository.save(tx);

    return wallet;
  }
}


package demo.userwallet.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.EnumType;

@Entity
@Table(name = "wallet_tx")
public class WalletTxEntity {
  @Id private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "tx_type", nullable = false)
  private WalletTxType txType;

  @Column(name = "amount", precision = 19, scale = 2, nullable = false)
  private BigDecimal amount;

  @Column(name = "balance_after_amount", precision = 19, scale = 2, nullable = false)
  private BigDecimal balanceAfterAmount;

  @Column(name = "reference_id")
  private String referenceId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public WalletTxType getTxType() {
    return txType;
  }

  public void setTxType(WalletTxType txType) {
    this.txType = txType;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public BigDecimal getBalanceAfterAmount() {
    return balanceAfterAmount;
  }

  public void setBalanceAfterAmount(BigDecimal balanceAfterAmount) {
    this.balanceAfterAmount = balanceAfterAmount;
  }

  public String getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}


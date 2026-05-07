package demo.userwallet.api.wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class WalletResponses {
  private WalletResponses() {}

  public record WalletResponse(UUID userId, BigDecimal balance, Instant updatedAt) {}

  public record WalletTxResponse(
      UUID id,
      String txType,
      BigDecimal amount,
      BigDecimal balanceAfter,
      String referenceId,
      Instant createdAt) {}

  public record WalletWithTxResponse(WalletResponse wallet, List<WalletTxResponse> recentTx) {}
}


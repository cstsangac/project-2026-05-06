package demo.userwallet.api.wallet;

import demo.userwallet.application.WalletService;
import demo.userwallet.application.security.JwtPrincipal;
import demo.userwallet.api.wallet.WalletResponses.WalletResponse;
import demo.userwallet.api.wallet.WalletResponses.WalletTxResponse;
import demo.userwallet.api.wallet.WalletResponses.WalletWithTxResponse;
import jakarta.validation.Valid;
import demo.userwallet.infrastructure.jpa.WalletEntity;
import demo.userwallet.infrastructure.jpa.WalletTxEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {
  private final WalletService walletService;

  public WalletController(WalletService walletService) {
    this.walletService = walletService;
  }

  @GetMapping
  public WalletWithTxResponse getWallet(Authentication authentication) {
    JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
    WalletEntity wallet = walletService.getWallet(principal.userId());
    List<WalletTxEntity> tx = walletService.recentTx(principal.userId(), 20);
    return new WalletWithTxResponse(
        new WalletResponse(wallet.getUserId(), wallet.getBalanceAmount(), wallet.getUpdatedAt()),
        tx.stream()
            .map(
                t ->
                    new WalletTxResponse(
                        t.getId(),
                        t.getTxType().name(),
                        t.getAmount(),
                        t.getBalanceAfterAmount(),
                        t.getReferenceId(),
                        t.getCreatedAt()))
            .toList());
  }

  @PostMapping("/topup")
  public WalletResponse topUp(
      Authentication authentication, @Valid @RequestBody TopUpRequest req) {
    JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
    String ref = "mock-payment-" + UUID.randomUUID();
    WalletEntity wallet = walletService.topUp(principal.userId(), req.amount(), ref);
    return new WalletResponse(wallet.getUserId(), wallet.getBalanceAmount(), wallet.getUpdatedAt());
  }
}


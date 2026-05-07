package demo.userwallet.api.gift;

import demo.userwallet.application.GiftService;
import demo.userwallet.application.security.JwtPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/streams/{streamId}/gifts")
public class GiftController {
  private final GiftService giftService;

  public GiftController(GiftService giftService) {
    this.giftService = giftService;
  }

  @PostMapping
  public SendGiftResponse sendGift(
      Authentication authentication,
      @PathVariable UUID streamId,
      @Valid @RequestBody SendGiftRequest req) {
    JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
    demo.userwallet.infrastructure.jpa.GiftEventEntity gift =
        giftService.sendGift(
            principal.userId(), streamId, req.giftType().trim().toUpperCase(), req.clientRequestId());
    return new SendGiftResponse(
        gift.getId(),
        gift.getStreamId(),
        gift.getGiftType(),
        gift.getGiftPrice(),
        gift.getCreatedAt());
  }
}


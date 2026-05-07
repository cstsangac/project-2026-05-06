package demo.userwallet.api.gift;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SendGiftResponse(
    UUID giftEventId,
    UUID streamId,
    String giftType,
    BigDecimal giftPrice,
    Instant createdAt) {}


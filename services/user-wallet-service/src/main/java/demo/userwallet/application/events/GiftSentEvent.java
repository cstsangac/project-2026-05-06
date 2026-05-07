package demo.userwallet.application.events;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.UUID;

public record GiftSentEvent(
    UUID giftEventId,
    UUID streamId,
    UUID senderUserId,
    String senderUsername,
    String giftType,
    BigDecimal giftPrice,
    Instant sentAt) {}


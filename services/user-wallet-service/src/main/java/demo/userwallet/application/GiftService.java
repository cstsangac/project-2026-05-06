package demo.userwallet.application;

import demo.userwallet.application.events.GiftSentEvent;
import demo.userwallet.domain.GiftCatalog;
import demo.userwallet.infrastructure.jpa.AppUserRepository;
import demo.userwallet.infrastructure.jpa.AppUserEntity;
import demo.userwallet.infrastructure.jpa.GiftEventEntity;
import demo.userwallet.infrastructure.jpa.GiftEventRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GiftService {
  private final GiftEventRepository giftEventRepository;
  private final AppUserRepository userRepository;
  private final WalletService walletService;
  private final KafkaTemplate<String, GiftSentEvent> kafkaTemplate;
  private final String topicGiftSent;
  private final StringRedisTemplate redisTemplate;
  private final boolean leaderboardEnabled;
  private static final int LEADERBOARD_SCORE_SCALE = 100; // store minor-units as integer in Redis score

  public GiftService(
      GiftEventRepository giftEventRepository,
      AppUserRepository userRepository,
      WalletService walletService,
      KafkaTemplate<String, GiftSentEvent> kafkaTemplate,
      @Value("${app.kafka.topic-gift-sent}") String topicGiftSent,
      StringRedisTemplate redisTemplate,
      @Value("${app.leaderboard.enabled}") boolean leaderboardEnabled) {
    this.giftEventRepository = giftEventRepository;
    this.userRepository = userRepository;
    this.walletService = walletService;
    this.kafkaTemplate = kafkaTemplate;
    this.topicGiftSent = topicGiftSent;
    this.redisTemplate = redisTemplate;
    this.leaderboardEnabled = leaderboardEnabled;
  }

  @Transactional
  public GiftEventEntity sendGift(
      UUID senderUserId, UUID streamId, String giftType, String clientRequestId) {
    if (clientRequestId != null && !clientRequestId.isBlank()) {
      Optional<GiftEventEntity> existing =
          giftEventRepository.findBySenderUserIdAndStreamIdAndClientRequestId(
              senderUserId, streamId, clientRequestId);
      if (existing.isPresent()) {
        return existing.get(); // idempotent reply
      }
    }

    AppUserEntity sender =
        userRepository
            .findById(senderUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    BigDecimal price = GiftCatalog.price(giftType);

    // Debit first (same transaction boundary).
    walletService.debitForGift(senderUserId, price, "stream=" + streamId + ",gift=" + giftType);

    GiftEventEntity gift = new GiftEventEntity();
    gift.setId(UUID.randomUUID());
    gift.setStreamId(streamId);
    gift.setSenderUserId(senderUserId);
    gift.setGiftType(giftType);
    gift.setGiftPrice(price);
    gift.setClientRequestId(clientRequestId != null && clientRequestId.isBlank() ? null : clientRequestId);
    gift.setCreatedAt(Instant.now());
    giftEventRepository.save(gift);

    if (leaderboardEnabled) {
      String key = "leaderboard:" + streamId;
      // Redis sorted-set scores are IEEE-754 doubles, so we store a scaled integer (minor units) to avoid decimal drift.
      long minorUnits =
          price.multiply(BigDecimal.valueOf(LEADERBOARD_SCORE_SCALE))
              .setScale(0, RoundingMode.UNNECESSARY)
              .longValueExact();
      redisTemplate.opsForZSet().incrementScore(key, sender.getId().toString(), minorUnits);
    }

    GiftSentEvent event =
        new GiftSentEvent(
            gift.getId(),
            streamId,
            senderUserId,
            sender.getUsername(),
            giftType,
            price,
            gift.getCreatedAt());
    try {
      kafkaTemplate.send(topicGiftSent, streamId.toString(), event).get(5, TimeUnit.SECONDS);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to publish gift event to Kafka", e);
    }

    return gift;
  }
}


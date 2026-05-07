package demo.userwallet.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "gift_event")
public class GiftEventEntity {
  @Id private UUID id;

  @Column(name = "stream_id", nullable = false)
  private UUID streamId;

  @Column(name = "sender_user_id", nullable = false)
  private UUID senderUserId;

  @Column(name = "gift_type", nullable = false)
  private String giftType;

  @Column(name = "gift_price", precision = 19, scale = 2, nullable = false)
  private BigDecimal giftPrice;

  @Column(name = "client_request_id")
  private String clientRequestId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getStreamId() {
    return streamId;
  }

  public void setStreamId(UUID streamId) {
    this.streamId = streamId;
  }

  public UUID getSenderUserId() {
    return senderUserId;
  }

  public void setSenderUserId(UUID senderUserId) {
    this.senderUserId = senderUserId;
  }

  public String getGiftType() {
    return giftType;
  }

  public void setGiftType(String giftType) {
    this.giftType = giftType;
  }

  public BigDecimal getGiftPrice() {
    return giftPrice;
  }

  public void setGiftPrice(BigDecimal giftPrice) {
    this.giftPrice = giftPrice;
  }

  public String getClientRequestId() {
    return clientRequestId;
  }

  public void setClientRequestId(String clientRequestId) {
    this.clientRequestId = clientRequestId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}


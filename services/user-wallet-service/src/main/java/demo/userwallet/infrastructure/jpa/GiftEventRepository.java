package demo.userwallet.infrastructure.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GiftEventRepository extends JpaRepository<GiftEventEntity, UUID> {
  Optional<GiftEventEntity> findBySenderUserIdAndStreamIdAndClientRequestId(
      UUID senderUserId, UUID streamId, String clientRequestId);
}


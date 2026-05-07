package demo.userwallet.infrastructure.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletTxRepository extends JpaRepository<WalletTxEntity, UUID> {
  @Query("select t from WalletTxEntity t where t.userId = :userId order by t.createdAt desc")
  List<WalletTxEntity> findRecentByUserId(@Param("userId") UUID userId, Pageable pageable);
}


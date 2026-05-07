package demo.userwallet;

import demo.userwallet.application.AuthService;
import demo.userwallet.application.GiftService;
import demo.userwallet.application.WalletService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(
    properties = {
      "app.leaderboard.enabled=false",
      "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
    })
@EmbeddedKafka(partitions = 1, topics = {"gift.sent"})
@EnabledIfEnvironmentVariable(named = "RUN_TESTCONTAINERS", matches = "true")
class SmokeIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:17").withDatabaseName("gifts").withUsername("gifts").withPassword("gifts");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.flyway.enabled", () -> true);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
  }

  // not needed when leaderboard is disabled, but Spring must still create the bean graph
  @MockBean StringRedisTemplate redisTemplate;

  @Autowired AuthService authService;
  @Autowired WalletService walletService;
  @Autowired GiftService giftService;

  @Test
  void registerTopupSendGift_smoke() {
    UUID userId = authService.register("demo-user-" + UUID.randomUUID(), "pw");
    walletService.topUp(userId, new BigDecimal("100.00"), "test-topup");
    giftService.sendGift(userId, UUID.randomUUID(), "ROSE", "req-1");
  }
}


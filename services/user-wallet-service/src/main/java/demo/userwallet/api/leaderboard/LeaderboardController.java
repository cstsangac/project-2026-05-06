package demo.userwallet.api.leaderboard;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/streams/{streamId}/leaderboard")
public class LeaderboardController {
  private final StringRedisTemplate redisTemplate;
  private final boolean leaderboardEnabled;

  public LeaderboardController(
      StringRedisTemplate redisTemplate,
      @Value("${app.leaderboard.enabled}") boolean leaderboardEnabled) {
    this.redisTemplate = redisTemplate;
    this.leaderboardEnabled = leaderboardEnabled;
  }

  @GetMapping
  public LeaderboardResponse top(@PathVariable UUID streamId) {
    if (!leaderboardEnabled) {
      return new LeaderboardResponse(streamId, List.of());
    }
    String key = "leaderboard:" + streamId;
    Set<ZSetOperations.TypedTuple<String>> tuples =
        redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 9);
    if (tuples == null) {
      return new LeaderboardResponse(streamId, List.of());
    }
    List<LeaderboardItem> items =
        tuples.stream()
            .map(
                t ->
                    new LeaderboardItem(
                        t.getValue() == null ? "" : t.getValue(),
                        t.getScore() == null ? 0L : t.getScore().longValue()))
            .toList();
    return new LeaderboardResponse(streamId, items);
  }
}


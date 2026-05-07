package demo.userwallet.api.leaderboard;

import java.util.List;
import java.util.UUID;

public record LeaderboardResponse(UUID streamId, List<LeaderboardItem> top) {}


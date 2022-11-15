package com.kenzie.caching.leaderboard;

import com.kenzie.caching.leaderboard.resources.GameServer;
import com.kenzie.caching.leaderboard.resources.StartGameRequest;
import com.kenzie.caching.leaderboard.resources.datasource.LeaderboardDao;
import com.kenzie.caching.leaderboard.resources.modules.ClientComponent;
import com.kenzie.caching.leaderboard.resources.modules.DaggerClientComponent;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntegrationTest {
    private static final ClientComponent component = DaggerClientComponent.create();
    private static final String USERNAME = "okayPlayer92";

    @Test
    void invalidate_whenKeyExists_deletesKey() {
        // GIVEN
        // preload the cache
        CacheClient cacheClient = component.buildClient();
        CachingLeaderboardDao cachingLeaderboardDao = new CachingLeaderboardDao(new LeaderboardDao(), cacheClient);
        cachingLeaderboardDao.getHighScore(USERNAME);
        // form the new request to start game
        StartGameActivity activity = new StartGameActivity(new GameServer(), cachingLeaderboardDao);
        StartGameRequest request = new StartGameRequest(USERNAME);

        // WHEN
        activity.enact(request);

        // THEN subsequent request for high score should not be cached and take a while to return
        Instant start = Instant.now();
        cachingLeaderboardDao.getHighScore(USERNAME);
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        assertTrue(duration.getSeconds() >= 5,
                    String.format("Enacting the StartGameActivity should invalidate entry for username " +
                                      "[%s] in the cache.",
                                  USERNAME));
    }
}

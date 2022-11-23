package com.kenzie.caching.leaderboard;

import com.kenzie.caching.leaderboard.resources.datasource.Entry;
import com.kenzie.caching.leaderboard.resources.datasource.LeaderboardDao;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

public class CachingLeaderboardDao {
    private final LeaderboardDao dataSource;
    private final CacheClient cache;

    /**
     * Constructor.
     *
     * @param dataSource LeaderboardDAO object
     * @param cache      CacheClient object
     */
    @Inject
    public CachingLeaderboardDao(LeaderboardDao dataSource, CacheClient cache) {
        this.dataSource = dataSource;
        this.cache = cache;
    }

    /**
     * Retrieves score associated with the specified user. Should use the cache when possible, but the dataSource object
     * is our source of truth for high scores. The TTL for our high scores should be 5 minutes.
     *
     * PARTICIPANTS: replace return 0 with your implementation of this method.
     *
     * @param username String representing player username
     * @return long representing score associated with username
     */
    public long getHighScore(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        long highScore = cache.getValue(username)
                .map(Long::valueOf)
                .orElseGet(() -> dataSource.getEntry(username).getScore());

            cache.setValue(username, 300, String.valueOf(highScore));
            return highScore;
    }
}

package com.kenzie.caching.goodreads.integration;


import com.kenzie.caching.goodreads.activity.GetGoalProgressActivity;
import com.kenzie.caching.goodreads.activity.MarkBookAsCurrentlyReadingActivity;
import com.kenzie.caching.goodreads.activity.MarkBookAsReadActivity;
import com.kenzie.caching.goodreads.activity.SetGoalActivity;
import com.kenzie.caching.goodreads.activity.UpdateReadingProgressActivity;
import com.kenzie.caching.goodreads.integration.helper.ActivityProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Phase4Test {
    private static final String USER_ID = "425c0e68-717c-49ef-9405-259453a5180d";
    private static final int YEAR = ZonedDateTime.now().getYear();
    private static final int GOAL_NUM_OF_BOOKS = 10;

    private GetGoalProgressActivity getGoalProgressActivity;
    private JedisPool jedisPool;

    @BeforeEach
    public void setup() {
        SetGoalActivity setGoalActivity = ActivityProvider.provideSetGoalActivity();
        setGoalActivity.handleRequest(USER_ID, YEAR, GOAL_NUM_OF_BOOKS);

        getGoalProgressActivity = ActivityProvider.provideGetGoalProgressActivity();
        jedisPool = ActivityProvider.provideJedisPool();
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.flushAll();
        }
        // Create a cache entry
        getGoalProgressActivity.handleRequest(USER_ID, YEAR);
    }

    @Test
    public void invalidateCache_markBookAsRead_emptyCache() {
        // GIVEN
        MarkBookAsReadActivity markBookAsReadActivity = ActivityProvider.provideMarkBookAsReadActivity();

        // WHEN
        markBookAsReadActivity.handleRequest(USER_ID, "9780060736262", ZonedDateTime.now(), 493);

        // THEN
        try (Jedis jedis = jedisPool.getResource()) {
            assertEquals(0, jedis.dbSize(),
                    "There should not be a value in the cache after a user has marked a new book as read.");
        }
    }

    @Test
    public void invalidateCache_updateReadingFinished_emptyCache() {
        // GIVEN
        UpdateReadingProgressActivity updateReadingProgressActivity = ActivityProvider.provideUpdateReadingProgressActivity();

        // WHEN
        updateReadingProgressActivity.handleRequest(USER_ID, "9780060736262", ZonedDateTime.now(), 493, true);

        // THEN
        try (Jedis jedis = jedisPool.getResource()) {
            assertEquals(0, jedis.dbSize(),
                    "There should not be a value in the cache after a user has marked a new book as read.");
        }
    }

    @Test
    public void doNotInvalidateCache_getGoalProgress_itemInCacheCache() {
        // GIVEN

        // WHEN
        getGoalProgressActivity.handleRequest(USER_ID, YEAR);

        // THEN
        try (Jedis jedis = jedisPool.getResource()) {
            assertEquals(1, jedis.dbSize(),
                    "The cache should not be invalidated when a customer finds out their goal progress.");
        }
    }

    @Test
    public void doNotInvalidateCache_markBookAsCurrentlyReading_itemInCacheCache() {
        // GIVEN
        MarkBookAsCurrentlyReadingActivity markBookAsCurrentlyReadingActivity = ActivityProvider.provideMarkBookAsCurrentlyReadingActivity();


        // WHEN
        markBookAsCurrentlyReadingActivity.handleRequest(USER_ID, "9780060736262", ZonedDateTime.now());

        // THEN
        try (Jedis jedis = jedisPool.getResource()) {
            assertEquals(1, jedis.dbSize(),
                    "The cache should not be invalidated when a customer starts a new book.");
        }
    }

    @Test
    public void doNotInvalidateCache_setGoal_itemInCacheCache() {
        // GIVEN
        SetGoalActivity setGoalActivity = ActivityProvider.provideSetGoalActivity();


        // WHEN
        setGoalActivity.handleRequest(USER_ID, YEAR, 12);

        // THEN
        try (Jedis jedis = jedisPool.getResource()) {
            assertEquals(1, jedis.dbSize(),
                    "The cache should not be invalidated when a customer sets a reading goal.");
        }
    }

    @Test
    public void doNotInvalidateCache_updateReadingNoFinished_itemInCacheCache() {
        // GIVEN
        UpdateReadingProgressActivity updateReadingProgressActivity = ActivityProvider.provideUpdateReadingProgressActivity();

        // WHEN
        updateReadingProgressActivity.handleRequest(USER_ID, "9780060736262", ZonedDateTime.now(), 100, false);

        // THEN
        try (Jedis jedis = jedisPool.getResource()) {
            assertEquals(1, jedis.dbSize(),
                    "The cache should not be invalidated when a customer is still reading a book.");
        }
    }
}

package com.kenzie.caching.goodreads.caching;


import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.kenzie.caching.goodreads.dao.NonCachingReadingLogDao;
import com.kenzie.caching.goodreads.dao.ReadingLogDao;
import com.kenzie.caching.goodreads.dao.models.ReadingLog;
import com.kenzie.caching.goodreads.dependency.DaoModule;

import java.time.ZonedDateTime;
import javax.inject.Inject;

public class CachingReadingLogDao implements ReadingLogDao {

    private NonCachingReadingLogDao readingLogDao = new NonCachingReadingLogDao(new DaoModule().provideDynamoDBMapper());
    private CacheClient cacheClient;

    @Inject
    public CachingReadingLogDao(CacheClient cacheClient) {
        this.cacheClient = cacheClient;
    }

    @Override
    public ReadingLog updateReadingProgress(String userId, String isbn, ZonedDateTime timestamp,
                                            int pageNumber, boolean isFinished) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        return readingLogDao.updateReadingProgress(userId, isbn, timestamp, pageNumber, isFinished);
    }

    @Override
    public int getBooksReadInYear(String userId, int year) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }

        int booksRead = cacheClient.getValue(userId)
                .map(Integer::valueOf)
                .orElseGet(() -> readingLogDao.getBooksReadInYear(userId, year));
        cacheClient.setValue(userId, 3600, String.valueOf(booksRead));

        return booksRead;
    }
}

package com.kenzie.caching.goodreads.activity;

import com.kenzie.caching.goodreads.caching.CachingReadingLogDao;
import com.kenzie.caching.goodreads.dao.ReadingLogDao;
import com.kenzie.caching.goodreads.dao.models.ReadingLog;

import java.time.ZonedDateTime;
import javax.inject.Inject;

/**
 * Handles requests to mark a book as read for a user.
 */
public class MarkBookAsReadActivity {

    private final CachingReadingLogDao cachingReadingLogDao;

    /**
     * Constructs an Activity with the given DAOs.
     * @param cachingReadingLogDao The ReadingLogDao to use for updating what a user has read
     */
    @Inject
    public MarkBookAsReadActivity(final CachingReadingLogDao cachingReadingLogDao) {
        this.cachingReadingLogDao = cachingReadingLogDao;
    }

    /**
     * Marks the provided book as read for a user.
     * @param userId - the user who has finished the book
     * @param isbn - the id of the book
     * @param timestamp - the the time to mark this book as having completed being read
     * @param numberPagesInBook - the total number of pages in the book
     *
     * @return the updated reading log for the book
     */
    public ReadingLog handleRequest(final String userId, final String isbn, final ZonedDateTime timestamp,
                                    final int numberPagesInBook) {
        return cachingReadingLogDao.updateReadingProgress(userId, isbn, timestamp, numberPagesInBook, true);
    }
}

package com.kenzie.caching.goodreads.caching;

import com.kenzie.caching.goodreads.dependency.CachingModule;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import java.util.Optional;

public class CacheClient {
    private final JedisPool pool;
    @Inject
    public CacheClient(JedisPool jedisPool) {
        this.pool = jedisPool;
    }

    public void setValue(String key, int seconds, String value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key or Value cannot be null");
        }

        try(Jedis jedis = pool.getResource()) {
            jedis.setex(key, seconds, value);
        }
    }

    public Optional<String> getValue(String key) {
        if(key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try (Jedis jedis = pool.getResource()) {
            String response = jedis.get(key);
            if(response != null) {
                return Optional.of(jedis.get(key));
            } else {
                return Optional.empty();
            }
        }
    }

    public boolean invalidate(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try (Jedis jedis = pool.getResource()) {
            Long response = jedis.del(key);
            if (response == 0) {
                return false;
            } else {
                return true;
            }
        }
    }
}

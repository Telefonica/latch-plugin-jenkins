package org.jenkinsci.plugins.latch;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class CsrfCache {

    private static final int EXPIRATATION_MINS = 2;

    private Cache cache;

    private static CsrfCache instance;

    public synchronized static CsrfCache getInstance() {
        if (instance == null) {
            instance = new CsrfCache();
        }
        return instance;
    }

    private CsrfCache() {
        cache = CacheBuilder.newBuilder()
                .maximumSize(32 * 1024l)
                .expireAfterWrite(EXPIRATATION_MINS, TimeUnit.MINUTES)
                .build();
    }

    public void put(String token, String userName) {
        cache.put(token, userName);
    }

    public synchronized boolean contains(String token, String username) {
        ConcurrentMap cacheMap = cache.asMap();
        return cacheMap.containsKey(token) && username.equals(cacheMap.get(token).toString());
    }

    public synchronized void clear(String token) {
        ConcurrentMap cacheMap = cache.asMap();
        if (cacheMap.containsKey(token)) {
            cacheMap.remove(token);
        }
    }
}

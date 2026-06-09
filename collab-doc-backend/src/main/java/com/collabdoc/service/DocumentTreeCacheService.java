package com.collabdoc.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentTreeCacheService {

    private final StringRedisTemplate redisTemplate;
    private final Gson gson = new Gson();

    private static final String CACHE_PREFIX = "doc:tree:";
    private static final long CACHE_TTL_SECONDS = 300;

    private final ConcurrentHashMap<Long, ReadWriteLock> workspaceLocks = new ConcurrentHashMap<>();
    // Primary: in-memory cache (always fast, no serialization issues)
    private final ConcurrentHashMap<Long, List<Map<String, Object>>> memoryCache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getOrLoad(Long workspaceId, Supplier<List<Map<String, Object>>> dbLoader) {
        // Fast path: in-memory cache
        List<Map<String, Object>> cached = memoryCache.get(workspaceId);
        if (cached != null) {
            return cached;
        }

        // Try Redis
        ReadWriteLock lock = workspaceLocks.computeIfAbsent(workspaceId, k -> new ReentrantReadWriteLock());
        String key = CACHE_PREFIX + workspaceId;

        lock.readLock().lock();
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                try {
                    List<Map<String, Object>> data = gson.fromJson(json, new TypeToken<List<Map<String, Object>>>() {}.getType());
                    memoryCache.put(workspaceId, data);
                    return data;
                } catch (Exception e) {
                    // Might be double-serialized JSON string — try parsing the string value
                    try {
                        String inner = gson.fromJson(json, String.class);
                        List<Map<String, Object>> data = gson.fromJson(inner, new TypeToken<List<Map<String, Object>>>() {}.getType());
                        memoryCache.put(workspaceId, data);
                        // Fix: re-write properly formatted data
                        redisTemplate.opsForValue().set(key, gson.toJson(data), CACHE_TTL_SECONDS);
                        return data;
                    } catch (Exception e2) {
                        log.warn("Redis cache corrupted for workspace {}, clearing", workspaceId);
                        redisTemplate.delete(key);
                    }
                }
            }
        } finally {
            lock.readLock().unlock();
        }

        // Cache miss — load from DB
        List<Map<String, Object>> data = dbLoader.get();
        memoryCache.put(workspaceId, data);

        // Write to Redis as backup (best effort)
        try {
            String serialized = gson.toJson(data);
            redisTemplate.opsForValue().set(key, serialized, CACHE_TTL_SECONDS);
        } catch (Exception e) {
            log.warn("Redis write failed for workspace {}: {}", workspaceId, e.getMessage());
        }

        return data;
    }

    public void invalidate(Long workspaceId) {
        memoryCache.remove(workspaceId);
        ReadWriteLock lock = workspaceLocks.computeIfAbsent(workspaceId, k -> new ReentrantReadWriteLock());
        lock.writeLock().lock();
        try {
            redisTemplate.delete(CACHE_PREFIX + workspaceId);
        } finally {
            lock.writeLock().unlock();
        }
    }
}

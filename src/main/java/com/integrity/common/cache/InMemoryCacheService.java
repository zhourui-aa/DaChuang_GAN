package com.integrity.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 进程内缓存实现（Caffeine）。P0 替代 Redis。
 */
@Service
public class InMemoryCacheService implements CacheService {

    private final Cache<String, Object> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .build();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        return (T) cache.getIfPresent(key);
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        // Caffeine 逐条 TTL 需 Expiry 策略；P0 统一走全局 TTL，逐条 TTL 在 Redis 实现中生效
        cache.put(key, value);
    }

    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public void evict(String key) {
        cache.invalidate(key);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Duration ttl, Supplier<T> loader) {
        Object v = cache.get(key, k -> loader.get());
        return (T) v;
    }
}

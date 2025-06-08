package ru.t1.accountservice.core.aop;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.t1.accountservice.core.annotation.Cached;

@Aspect
@Component
@Slf4j
public class CachedAspect {

    @Value("${t1.cache.ttl}")
    private Duration ttl;

    @Value("${t1.cache.max-size}")
    private long maxCacheSize;

    private final Map<NameAndArgs, CacheEntry> caches = new ConcurrentHashMap<>();

    @Around("@annotation(cached)")
    public Object cached(ProceedingJoinPoint proceedingJoinPoint, Cached cached) throws Throwable {
        long ttlMs = ttl.toMillis();
        String cacheName = cached.name();

        if (caches.size() > maxCacheSize * 0.8) {
            clearCacheForExpired();
        }

        NameAndArgs nameAndArgs = new NameAndArgs(cacheName, Arrays.toString(proceedingJoinPoint.getArgs()));
        if (caches.containsKey(nameAndArgs)) {
            CacheEntry cacheEntry = caches.get(nameAndArgs);
            if (isCacheExpired(cacheEntry, ttlMs)) {
                log.info("Cache return {} for {}", cacheEntry.value, nameAndArgs);
                return cacheEntry.value;
            } else {
                caches.remove(nameAndArgs);
            }
        }

        return computeAndCacheValue(proceedingJoinPoint, nameAndArgs, ttlMs);
    }

    private void clearCacheForExpired() {
        caches.entrySet()
                .removeIf(entry -> isCacheExpired(entry.getValue(), ttl.toMillis()));
    }

    private Object computeAndCacheValue(ProceedingJoinPoint proceedingJoinPoint, NameAndArgs nameAndArgs, long ttlMs) throws Throwable {
        Object result = proceedingJoinPoint.proceed();
        log.info("Cache save {} for {}", result, nameAndArgs);
        caches.put(nameAndArgs, new CacheEntry(System.currentTimeMillis(), result));
        return result;
    }

    private boolean isCacheExpired(CacheEntry cacheEntry, long ttlMs) {
        return System.currentTimeMillis() - cacheEntry.createTime > ttlMs;
    }

    record CacheEntry(
            long createTime,
            Object value
    ) {
    }

    record NameAndArgs(
            String name,
            String args
    ) {
    }
}

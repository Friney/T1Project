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

    @Value("${cache.ttl}")
    private Duration ttl;

    @Value("${cache.max-size}")
    private long maxCacheSize;

    private final Map<NameAndArgs, CacheEntry> cache = new ConcurrentHashMap<>();

    @Around("@annotation(cached)")
    public Object cached(ProceedingJoinPoint proceedingJoinPoint, Cached cached) throws Throwable {
        long ttlMs = ttl.toMillis();
        String cacheName = cached.name();

        if (cache.size() > maxCacheSize * 0.8) {
            clearCacheForExpired();
        }

        NameAndArgs nameAndArgs = new NameAndArgs(cacheName, Arrays.toString(proceedingJoinPoint.getArgs()));
        if (cache.containsKey(nameAndArgs)) {
            CacheEntry cacheEntry = cache.get(nameAndArgs);
            if (isCacheExpired(cacheEntry, ttlMs)) {
                log.info("Cache return {} for {}", cacheEntry.value, nameAndArgs);
                return cacheEntry.value;
            } else {
                cache.remove(nameAndArgs);
            }
        }

        return computeAndCacheValue(proceedingJoinPoint, nameAndArgs, ttlMs);
    }

    private void clearCacheForExpired() {
        cache.entrySet()
                .removeIf(entry -> isCacheExpired(entry.getValue(), ttl.toMillis()));
    }

    private Object computeAndCacheValue(ProceedingJoinPoint proceedingJoinPoint, NameAndArgs nameAndArgs, long ttlMs) throws Throwable {
        Object result = proceedingJoinPoint.proceed();
        log.info("Cache save {} for {}", result, nameAndArgs);
        cache.put(nameAndArgs, new CacheEntry(System.currentTimeMillis(), result));
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

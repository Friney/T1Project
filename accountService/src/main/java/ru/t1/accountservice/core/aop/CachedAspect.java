package ru.t1.accountservice.core.aop;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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

    private static final Map<NameAndArgs, CacheEntry> CACHE = new HashMap<>();

    @Around("@annotation(cached)")
    public Object cached(ProceedingJoinPoint proceedingJoinPoint, Cached cached) throws Throwable {
        long ttlMs = ttl.toMillis();
        String cacheName = cached.name();

        NameAndArgs nameAndArgs = new NameAndArgs(cacheName, Arrays.toString(proceedingJoinPoint.getArgs()));
        if (CACHE.containsKey(nameAndArgs)) {
            CacheEntry cacheEntry = CACHE.get(nameAndArgs);
            if (isCacheExpired(cacheEntry, ttlMs)) {
                log.info("Cache return {} for {}", cacheEntry.value, nameAndArgs);
                return cacheEntry.value;
            } else {
                CACHE.remove(nameAndArgs);
            }
        }

        return computeAndCacheValue(proceedingJoinPoint, nameAndArgs, ttlMs);
    }

    private Object computeAndCacheValue(ProceedingJoinPoint proceedingJoinPoint, NameAndArgs nameAndArgs, long ttlMs) throws Throwable {
        Object result = proceedingJoinPoint.proceed();
        log.info("Cache save {} for {}", result, nameAndArgs);
        CACHE.put(nameAndArgs, new CacheEntry(System.currentTimeMillis(), result));
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

package ru.t1.accountservice.core.aop;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.t1.accountservice.core.service.timelimit.TimeLimitExceedLogService;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MetricAspect {

    @Value("${metric.time-limit}")
    private Duration timeLimit;

    private final TimeLimitExceedLogService timeLimitExceedLogService;

    @Around("@annotation(ru.t1.accountservice.core.annotation.Metric)")
    public Object logMetric(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long timeLimitMs = timeLimit.toMillis();
        long beforeTime = System.currentTimeMillis();
        Object result;
        try {
            result = proceedingJoinPoint.proceed();
        } finally {
            long afterTime = System.currentTimeMillis();
            long executionTime = afterTime - beforeTime;
            if (executionTime > timeLimitMs) {
                log.warn("Method {} executed in {} ms, time limit exceeded. Limit is {} ms", proceedingJoinPoint.getSignature().getName(), executionTime, timeLimitMs);
                timeLimitExceedLogService.save(proceedingJoinPoint.getSignature().toString(), executionTime, timeLimitMs, LocalDateTime.now());
            }
        }

        return result;
    }
}

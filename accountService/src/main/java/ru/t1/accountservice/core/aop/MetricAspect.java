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
import ru.t1.accountservice.api.dto.timelimit.TimeLimitExceedLogDto;
import ru.t1.accountservice.core.kafka.KafkaMetricProducer;
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
    private final KafkaMetricProducer kafkaMetricProducer;

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
            TimeLimitExceedLogDto timeLimitExceedLogDto = TimeLimitExceedLogDto.builder()
                    .executionTime(executionTime)
                    .timeLimit(timeLimitMs)
                    .logTime(LocalDateTime.now())
                    .methodSignature(proceedingJoinPoint.getSignature().toString())
                    .build();

            if (executionTime > timeLimitMs) {
                log.warn("Method {} executed in {} ms, time limit exceeded. Limit is {} ms", proceedingJoinPoint.getSignature().getName(), executionTime, timeLimitMs);
                try {
                    kafkaMetricProducer.sendMessage(timeLimitExceedLogDto, "METRICS");
                    log.info("Log sent to Kafka");
                } catch (Exception e) {
                    log.error("Error sending log to Kafka -> {}", e.getMessage());
                    timeLimitExceedLogService.save(timeLimitExceedLogDto);
                    log.info("Log saved to DB");
                }
            }
        }

        return result;
    }
}

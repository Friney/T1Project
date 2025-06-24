package ru.t1.monitoringstarter.core.aop;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataAccessException;
import ru.t1.monitoringstarter.api.dto.datasource.DataSourceErrorLogDto;
import ru.t1.monitoringstarter.core.kafka.KafkaMetricProducer;
import ru.t1.monitoringstarter.core.service.datasource.DataSourceErrorLogService;

@Aspect
@RequiredArgsConstructor
@Slf4j
public class LogDataSourceErrorAspect {

    private final DataSourceErrorLogService dataSourceErrorLogService;
    private final KafkaMetricProducer kafkaMetricProducer;

    @AfterThrowing(value = "@annotation(ru.t1.monitoringstarter.core.annotation.LogDataSourceError)", throwing = "e")
    public void logDataSourceError(JoinPoint joinPoint, DataAccessException e) {
        try {
            DataSourceErrorLogDto dataSourceErrorLogDto = DataSourceErrorLogDto.builder()
                    .stackTrace(Arrays.toString(e.getStackTrace()))
                    .errorMessage(e.getMessage())
                    .methodSignature(joinPoint.getSignature().toString())
                    .build();

            try {
                kafkaMetricProducer.sendMessage(dataSourceErrorLogDto, "DATA_SOURCE");
                log.info("Log sent to Kafka");
            } catch (Exception ex) {
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    log.error("Interrupted while sending to Kafka", ex);
                }
                log.error("Error sending log to Kafka -> {}", ex.getMessage());
                dataSourceErrorLogService.save(dataSourceErrorLogDto);
                log.info("Log saved to DB");
            }
        } catch (RuntimeException ex) {
            log.error("Error while logging data source error -> {}", ex.getMessage());
        }
    }
}

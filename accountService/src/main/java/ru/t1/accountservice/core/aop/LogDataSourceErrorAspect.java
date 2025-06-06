package ru.t1.accountservice.core.aop;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import ru.t1.accountservice.api.dto.datasource.DataSourceErrorLogDto;
import ru.t1.accountservice.core.kafka.KafkaMetricProducer;
import ru.t1.accountservice.core.service.datasource.DataSourceErrorLogService;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LogDataSourceErrorAspect {

    private final DataSourceErrorLogService dataSourceErrorLogService;
    private final KafkaMetricProducer kafkaMetricProducer;

    @AfterThrowing(value = "@annotation(ru.t1.accountservice.core.annotation.LogDataSourceError)", throwing = "e")
    public void logDataSourceError(JoinPoint joinPoint, DataAccessException e) {
        try {
            DataSourceErrorLogDto dataSourceErrorLogDto = DataSourceErrorLogDto.builder()
                    .stackTrace(Arrays.toString(e.getStackTrace()))
                    .errorMessage(e.getMessage())
                    .methodSignature(joinPoint.getSignature().toString())
                    .build();

            try {
                kafkaMetricProducer.sendMessage(dataSourceErrorLogDto, "ERRORS");
                log.info("Log sent to Kafka");
            } catch (Exception ex) {
                log.error("Error sending log to Kafka -> {}", ex.getMessage());
                dataSourceErrorLogService.save(dataSourceErrorLogDto);
                log.info("Log saved to DB");
            }
        } catch (Exception ex) {
            log.error("Error while logging data source error -> {}", ex.getMessage());
        }
    }
}

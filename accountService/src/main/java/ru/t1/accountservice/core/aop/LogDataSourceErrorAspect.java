package ru.t1.accountservice.core.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import ru.t1.accountservice.core.service.datasource.DataSourceErrorLogService;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LogDataSourceErrorAspect {

    private final DataSourceErrorLogService dataSourceErrorLogService;

    @AfterThrowing(value = "@annotation(ru.t1.accountservice.core.annotation.LogDataSourceError)", throwing = "e")
    public void logDataSourceError(JoinPoint joinPoint, DataAccessException e) {
        try {
            dataSourceErrorLogService.save(joinPoint.getSignature().toString(), e.getMessage(), e.getMostSpecificCause().toString());
        } catch (Exception ex) {
            log.error("Error while logging data source error -> {}", ex.getMessage());
        }
    }
}

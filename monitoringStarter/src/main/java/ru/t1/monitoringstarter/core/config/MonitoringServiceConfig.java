package ru.t1.monitoringstarter.core.config;

import org.springframework.context.annotation.Bean;
import ru.t1.monitoringstarter.core.repository.DataSourceErrorLogRepository;
import ru.t1.monitoringstarter.core.repository.TimeLimitExceedLogRepository;
import ru.t1.monitoringstarter.core.service.datasource.DataSourceErrorLogService;
import ru.t1.monitoringstarter.core.service.datasource.DataSourceErrorLogServiceImpl;
import ru.t1.monitoringstarter.core.service.timelimit.TimeLimitExceedLogService;
import ru.t1.monitoringstarter.core.service.timelimit.TimeLimitExceedLogServiceImpl;

public class MonitoringServiceConfig {

    @Bean
    public TimeLimitExceedLogService timeLimitExceedLogService(TimeLimitExceedLogRepository timeLimitExceedLogRepository) {
        return new TimeLimitExceedLogServiceImpl(timeLimitExceedLogRepository);
    }

    @Bean
    public DataSourceErrorLogService dataSourceErrorLogService(DataSourceErrorLogRepository dataSourceErrorLogRepository) {
        return new DataSourceErrorLogServiceImpl(dataSourceErrorLogRepository);
    }
}

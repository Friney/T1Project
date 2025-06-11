package ru.t1.monitoringstarter.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.t1.monitoringstarter.core.repository.DataSourceErrorLogRepository;
import ru.t1.monitoringstarter.core.repository.TimeLimitExceedLogRepository;

@EnableJpaRepositories(basePackages = "ru.t1.monitoringstarter.core.repository")
public class MonitoringRepositoryConfig {

    @Bean
    public DataSourceErrorLogRepository dataSourceErrorLogRepository() {
        return null; // Spring Data JPA создаст реализацию автоматически
    }

    @Bean
    public TimeLimitExceedLogRepository timeLimitExceedLogRepository() {
        return null; // Spring Data JPA создаст реализацию автоматически
    }
}
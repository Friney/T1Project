package ru.t1.monitoringstarter.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(name = "t1.monitoring.repository.enabled", havingValue = "true", matchIfMissing = true)
@EntityScan(basePackages = {
        "ru.t1.monitoringstarter.core.entity",
        "ru.t1.accountservice.core.entity"
})
@EnableJpaRepositories(basePackages = "ru.t1.monitoringstarter.core.repository")
public class MonitoringRepositoryConfig {
}
package ru.t1.monitoringstarter.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(name = "t1.monitoring.repository.enabled", havingValue = "true", matchIfMissing = true)
@EnableJpaRepositories(basePackages = "ru.t1.monitoringstarter.core.repository")
@Import(MonitoringEntityRegistrar.class)
public class MonitoringRepositoryConfig {
}
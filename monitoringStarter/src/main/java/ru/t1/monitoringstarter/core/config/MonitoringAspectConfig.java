package ru.t1.monitoringstarter.core.config;

import org.springframework.context.annotation.Bean;
import ru.t1.monitoringstarter.core.aop.LogDataSourceErrorAspect;
import ru.t1.monitoringstarter.core.aop.MetricAspect;
import ru.t1.monitoringstarter.core.kafka.KafkaMetricProducer;
import ru.t1.monitoringstarter.core.service.datasource.DataSourceErrorLogService;
import ru.t1.monitoringstarter.core.service.timelimit.TimeLimitExceedLogService;

public class MonitoringAspectConfig {

    @Bean
    public LogDataSourceErrorAspect logDataSourceErrorAspect(
            DataSourceErrorLogService dataSourceErrorLogService,
            KafkaMetricProducer kafkaMetricProducer
    ) {
        return new LogDataSourceErrorAspect(dataSourceErrorLogService, kafkaMetricProducer);
    }

    @Bean
    public MetricAspect metricAspect(
            TimeLimitExceedLogService timeLimitExceedLogService,
            KafkaMetricProducer kafkaMetricProducer
    ) {
        return new MetricAspect(timeLimitExceedLogService, kafkaMetricProducer);
    }
}

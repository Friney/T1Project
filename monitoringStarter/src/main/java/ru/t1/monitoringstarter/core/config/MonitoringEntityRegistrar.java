package ru.t1.monitoringstarter.core.config;

import io.micrometer.common.lang.NonNull;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import ru.t1.monitoringstarter.core.entity.datasource.DataSourceErrorLog;
import ru.t1.monitoringstarter.core.entity.timelimit.TimeLimitExceedLog;

public class MonitoringEntityRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(
            @NonNull AnnotationMetadata importingClassMetadata,
            @NonNull BeanDefinitionRegistry registry
    ) {
        AutoConfigurationPackages.register(registry, DataSourceErrorLog.class.getPackageName());
        AutoConfigurationPackages.register(registry, TimeLimitExceedLog.class.getPackageName());
    }
}
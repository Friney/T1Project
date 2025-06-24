package ru.t1.monitoringstarter.core.config;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.t1.monitoringstarter.core.database.DatabaseSchemaValidator;

public class MonitoringJdbcConfig {

    @Bean
    @ConditionalOnMissingBean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public DatabaseSchemaValidator databaseSchemaValidator(JdbcTemplate jdbcTemplate) {
        DatabaseSchemaValidator databaseSchemaValidator = new DatabaseSchemaValidator(jdbcTemplate);
        databaseSchemaValidator.validateAndCreateTables();
        return databaseSchemaValidator;
    }
}

package ru.t1.monitoringstarter.core.database;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class DatabaseSchemaValidator {

    private final JdbcTemplate jdbcTemplate;

    public void validateAndCreateTables() {
        validateAndCreateTable(
                "data_source_error_log",
                """
                        create table if not exists data_source_error_log
                        (
                            id               bigserial primary key,
                            stack_trace      text not null,
                            error_message    text not null,
                            method_signature text not null
                        );"""
        );

        validateAndCreateTable(
                "time_limit_exceed_log",
                """
                        create table if not exists time_limit_exceed_log
                        (
                            id               bigserial primary key,
                            method_signature text      not null,
                            execution_time   bigint    not null,
                            time_limit       bigint    not null,
                            log_time         timestamp not null
                        );"""
        );
    }

    private void validateAndCreateTable(String tableName, String createTableSql) {
        if (!isTableExists(tableName)) {
            jdbcTemplate.execute(createTableSql);
        }
    }

    private boolean isTableExists(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?";
            jdbcTemplate.queryForObject(sql, Integer.class, tableName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

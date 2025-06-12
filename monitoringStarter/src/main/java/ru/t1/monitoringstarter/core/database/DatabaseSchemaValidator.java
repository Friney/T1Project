package ru.t1.monitoringstarter.core.database;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class DatabaseSchemaValidator {

    private final JdbcTemplate jdbcTemplate;

    public void validateAndCreateTables() {
        validateAndCreateTable(
                "data_source_error_log",
                "create table if not exists data_source_error_log\n" +
                        "(\n" +
                        "    id               bigserial primary key,\n" +
                        "    stack_trace      text not null,\n" +
                        "    error_message    text not null,\n" +
                        "    method_signature text not null\n" +
                        ");"
        );

        validateAndCreateTable(
                "time_limit_exceed_log",
                "create table if not exists time_limit_exceed_log\n" +
                        "(\n" +
                        "    id               bigserial primary key,\n" +
                        "    method_signature text      not null,\n" +
                        "    execution_time   bigint    not null,\n" +
                        "    time_limit       bigint    not null,\n" +
                        "    log_time         timestamp not null\n" +
                        ");"
        );
    }

    private void validateAndCreateTable(String tableName, String createTableSql) {
        if (!isTableExists(tableName)) {
            jdbcTemplate.execute(createTableSql);
        }
    }

    private boolean isTableExists(String tableName) {
        try {
            String query = "select 1 from " + tableName + " limit 1";
            jdbcTemplate.queryForObject(query, Integer.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
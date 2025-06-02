package ru.t1.accountservice.core.service.datasource;

public interface DataSourceErrorLogService {

    void save(String stackTrace, String errorMessage, String methodSignature);
}

package ru.t1.accountservice.core.service.datasource;

import ru.t1.accountservice.api.dto.datasource.DataSourceErrorLogDto;

public interface DataSourceErrorLogService {

    void save(DataSourceErrorLogDto dataSourceErrorLogDto);
}

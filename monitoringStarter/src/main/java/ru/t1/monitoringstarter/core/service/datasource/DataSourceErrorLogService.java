package ru.t1.monitoringstarter.core.service.datasource;

import ru.t1.monitoringstarter.api.dto.datasource.DataSourceErrorLogDto;

public interface DataSourceErrorLogService {

    void save(DataSourceErrorLogDto dataSourceErrorLogDto);
}

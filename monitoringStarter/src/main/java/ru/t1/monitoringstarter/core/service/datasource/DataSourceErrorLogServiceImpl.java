package ru.t1.monitoringstarter.core.service.datasource;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.monitoringstarter.api.dto.datasource.DataSourceErrorLogDto;
import ru.t1.monitoringstarter.core.entity.datasource.DataSourceErrorLog;
import ru.t1.monitoringstarter.core.repository.DataSourceErrorLogRepository;

@RequiredArgsConstructor
public class DataSourceErrorLogServiceImpl implements DataSourceErrorLogService {

    private final DataSourceErrorLogRepository dataSourceErrorLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(DataSourceErrorLogDto dataSourceErrorLogDto) {
        DataSourceErrorLog dataSourceErrorLog = DataSourceErrorLog.builder()
                .stackTrace(dataSourceErrorLogDto.stackTrace())
                .errorMessage(dataSourceErrorLogDto.errorMessage())
                .methodSignature(dataSourceErrorLogDto.methodSignature())
                .build();

        dataSourceErrorLogRepository.save(dataSourceErrorLog);
    }
}

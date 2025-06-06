package ru.t1.accountservice.core.service.datasource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.accountservice.api.dto.datasource.DataSourceErrorLogDto;
import ru.t1.accountservice.core.entity.datasource.DataSourceErrorLog;
import ru.t1.accountservice.core.repository.DataSourceErrorLogRepository;

@Service
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

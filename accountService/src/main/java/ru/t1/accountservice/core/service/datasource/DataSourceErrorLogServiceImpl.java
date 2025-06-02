package ru.t1.accountservice.core.service.datasource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.accountservice.core.entity.datasource.DataSourceErrorLog;
import ru.t1.accountservice.core.repository.DataSourceErrorLogRepository;

@Service
@RequiredArgsConstructor
public class DataSourceErrorLogServiceImpl implements DataSourceErrorLogService {

    private final DataSourceErrorLogRepository dataSourceErrorLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(String stackTrace, String errorMessage, String methodSignature) {
        DataSourceErrorLog dataSourceErrorLog = DataSourceErrorLog.builder()
                .stackTrace(stackTrace)
                .errorMessage(errorMessage)
                .methodSignature(methodSignature)
                .build();

        dataSourceErrorLogRepository.save(dataSourceErrorLog);
    }
}

package ru.t1.accountservice.core.service.timelimit;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.accountservice.core.annotation.LogDataSourceError;
import ru.t1.accountservice.core.entity.timelimit.TimeLimitExceedLog;
import ru.t1.accountservice.core.repository.TimeLimitExceedLogRepository;

@Service
@RequiredArgsConstructor
public class TimeLimitExceedLogServiceImpl implements TimeLimitExceedLogService {

    private final TimeLimitExceedLogRepository timeLimitExceedLogRepository;

    @Override
    @LogDataSourceError
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(String methodSignature, long executionTime, long timeLimit, LocalDateTime logTime) {
        TimeLimitExceedLog timeLimitExceedLog = TimeLimitExceedLog.builder()
                .methodSignature(methodSignature)
                .executionTime(executionTime)
                .timeLimit(timeLimit)
                .logTime(logTime)
                .build();

        timeLimitExceedLogRepository.save(timeLimitExceedLog);
    }
}

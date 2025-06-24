package ru.t1.monitoringstarter.core.service.timelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.monitoringstarter.api.dto.timelimit.TimeLimitExceedLogDto;
import ru.t1.monitoringstarter.core.annotation.LogDataSourceError;
import ru.t1.monitoringstarter.core.entity.timelimit.TimeLimitExceedLog;
import ru.t1.monitoringstarter.core.repository.TimeLimitExceedLogRepository;

@RequiredArgsConstructor
public class TimeLimitExceedLogServiceImpl implements TimeLimitExceedLogService {

    private final TimeLimitExceedLogRepository timeLimitExceedLogRepository;

    @Override
    @LogDataSourceError
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(TimeLimitExceedLogDto timeLimitExceedLogDto) {
        TimeLimitExceedLog timeLimitExceedLog = TimeLimitExceedLog.builder()
                .methodSignature(timeLimitExceedLogDto.methodSignature())
                .executionTime(timeLimitExceedLogDto.executionTime())
                .timeLimit(timeLimitExceedLogDto.timeLimit())
                .logTime(timeLimitExceedLogDto.logTime())
                .build();

        timeLimitExceedLogRepository.save(timeLimitExceedLog);
    }
}

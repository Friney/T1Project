package ru.t1.accountservice.core.service.timelimit;

import java.time.LocalDateTime;

public interface TimeLimitExceedLogService {

    void save(String methodSignature, long executionTime, long timeLimit, LocalDateTime logTime);
}

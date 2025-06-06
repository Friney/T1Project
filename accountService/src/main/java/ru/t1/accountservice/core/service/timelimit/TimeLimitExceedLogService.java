package ru.t1.accountservice.core.service.timelimit;

import ru.t1.accountservice.api.dto.timelimit.TimeLimitExceedLogDto;

public interface TimeLimitExceedLogService {

    void save(TimeLimitExceedLogDto timeLimitExceedLogDto);
}

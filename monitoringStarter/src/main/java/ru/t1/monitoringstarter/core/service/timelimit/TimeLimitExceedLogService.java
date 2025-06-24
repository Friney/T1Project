package ru.t1.monitoringstarter.core.service.timelimit;

import ru.t1.monitoringstarter.api.dto.timelimit.TimeLimitExceedLogDto;

public interface TimeLimitExceedLogService {

    void save(TimeLimitExceedLogDto timeLimitExceedLogDto);
}

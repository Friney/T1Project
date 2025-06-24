package ru.t1.monitoringstarter.api.dto.timelimit;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record TimeLimitExceedLogDto(
        String methodSignature,
        Long executionTime,
        Long timeLimit,
        LocalDateTime logTime
) {
}

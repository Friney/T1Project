package ru.t1.monitoringstarter.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.monitoringstarter.core.entity.timelimit.TimeLimitExceedLog;

public interface TimeLimitExceedLogRepository extends JpaRepository<TimeLimitExceedLog, Long> {
}

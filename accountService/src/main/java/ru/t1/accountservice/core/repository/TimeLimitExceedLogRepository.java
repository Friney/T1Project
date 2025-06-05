package ru.t1.accountservice.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.t1.accountservice.core.entity.timelimit.TimeLimitExceedLog;

@Repository
public interface TimeLimitExceedLogRepository extends JpaRepository<TimeLimitExceedLog, Long> {
}

package ru.t1.monitoringstarter.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.monitoringstarter.core.entity.datasource.DataSourceErrorLog;

public interface DataSourceErrorLogRepository extends JpaRepository<DataSourceErrorLog, Long> {
}

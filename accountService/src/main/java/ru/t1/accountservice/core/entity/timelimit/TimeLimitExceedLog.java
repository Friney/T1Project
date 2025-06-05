package ru.t1.accountservice.core.entity.timelimit;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@Entity
@Table(name = "time_limit_exceed_log")
@AllArgsConstructor
@NoArgsConstructor
public class TimeLimitExceedLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String methodSignature;

    private Long executionTime;

    private Long timeLimit;

    private LocalDateTime logTime;
}

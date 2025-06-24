package ru.t1.accountservice.core.entity.transaction;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.t1.accountservice.core.entity.account.Account;


@Builder
@Getter
@Setter
@Entity
@Table(name = "transaction")
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private LocalDateTime transactionTime;

    private TransactionStatus status;

    private LocalDateTime createTime;

    private Long transactionId;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

}

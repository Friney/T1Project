package ru.t1.accountservice.core.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.t1.accountservice.core.entity.transaction.Transaction;
import ru.t1.accountservice.core.entity.transaction.TransactionStatus;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByAccountId(Long accountId);

    List<Transaction> findAllByAccountIdAndStatus(Long accountId, TransactionStatus status);

    void deleteByTransactionId(Long transactionId);

    Optional<Transaction> findByTransactionIdAndAccountId(Long transactionId, Long accountId);

    @Query(value = "SELECT nextval('transaction_end_to_end_id_seq')")
    Long getNextTransactionId();
}

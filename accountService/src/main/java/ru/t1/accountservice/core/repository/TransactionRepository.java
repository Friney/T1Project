package ru.t1.accountservice.core.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.t1.accountservice.core.entity.transaction.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndAccountId(Long id, Long accountId);

    List<Transaction> findAllByAccountId(Long accountId);
}

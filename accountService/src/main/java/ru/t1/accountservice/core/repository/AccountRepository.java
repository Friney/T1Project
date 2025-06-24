package ru.t1.accountservice.core.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.t1.accountservice.core.entity.account.Account;
import ru.t1.accountservice.core.entity.account.AccountStatus;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByAccountId(Long accountId);

    Optional<Account> findByAccountIdAndClientId(Long accountId, Long clientId);

    void deleteByAccountId(Long accountId);

    List<Account> findAllByClientId(Long clientId);

    List<Account> findAllByStatus(AccountStatus status);

    Optional<Account> findByAccountId(Long accountId);

    @Query(value = "SELECT nextval('account_end_to_end_id_seq')")
    Long getNextAccountId();
}

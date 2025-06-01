package ru.t1.accountservice.core.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.t1.accountservice.core.entity.account.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByIdAndClientId(Long id, Long clientId);

    List<Account> findAllByClientId(Long clientId);
}

package ru.t1.accountservice.core.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.t1.accountservice.core.entity.client.Client;
import ru.t1.accountservice.core.entity.client.ClientStatus;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    void deleteByClientId(Long clientId);

    boolean existsByClientId(Long clientId);

    Optional<Client> findByClientId(Long clientId);

    List<Client> findAllByStatus(ClientStatus status);

    @Query(value = "SELECT nextval('client_end_to_end_id_seq')")
    Long getNextClientId();
}

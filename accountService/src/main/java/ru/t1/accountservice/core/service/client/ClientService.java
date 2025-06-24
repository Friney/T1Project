package ru.t1.accountservice.core.service.client;

import java.util.List;
import ru.t1.accountservice.api.dto.client.ClientCreateRequest;
import ru.t1.accountservice.api.dto.client.ClientDto;
import ru.t1.accountservice.api.dto.client.ClientUpdateRequest;
import ru.t1.accountservice.core.entity.client.ClientStatus;

public interface ClientService {

    List<ClientDto> getAll();

    List<ClientDto> getAllByStatus(ClientStatus status);

    ClientStatus getStatus(long id);

    ClientDto getById(long id);

    ClientDto create(ClientCreateRequest clientCreateRequest);

    ClientDto update(ClientUpdateRequest clientUpdateRequest, long id);

    void updateStatus(long id, ClientStatus status);

    void delete(long id);

    boolean existsById(long id);
}

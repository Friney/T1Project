package ru.t1.accountservice.core.service.client;

import java.util.List;
import ru.t1.accountservice.api.dto.client.ClientCreateRequest;
import ru.t1.accountservice.api.dto.client.ClientDto;
import ru.t1.accountservice.api.dto.client.ClientUpdateRequest;

public interface ClientService {

    List<ClientDto> getAll();

    ClientDto getById(long id);

    ClientDto create(ClientCreateRequest clientCreateRequest);

    ClientDto update(long id, ClientUpdateRequest clientUpdateRequest);

    void delete(long id);

}

package ru.t1.accountservice.core.service.client;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.accountservice.api.dto.client.ClientCreateRequest;
import ru.t1.accountservice.api.dto.client.ClientDto;
import ru.t1.accountservice.api.dto.client.ClientUpdateRequest;
import ru.t1.accountservice.core.annotation.Cached;
import ru.t1.accountservice.core.annotation.LogDataSourceError;
import ru.t1.accountservice.core.annotation.Metric;
import ru.t1.accountservice.core.entity.client.Client;
import ru.t1.accountservice.core.exception.ServiceException;
import ru.t1.accountservice.core.mapper.ClientMapper;
import ru.t1.accountservice.core.repository.ClientRepository;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ClientDto> getAll() {
        return clientMapper.map(clientRepository.findAll());
    }

    @Override
    @Cached(name = "client")
    @Transactional(readOnly = true)
    public ClientDto getById(long id) {
        return clientMapper.map(getEntityById(id));
    }

    @Override
    @Metric
    @LogDataSourceError
    @Transactional
    public ClientDto create(ClientCreateRequest clientCreateRequest) {

        Client client = Client.builder()
                .clientId(clientRepository.getNextClientId())
                .firstName(clientCreateRequest.firstName())
                .lastName(clientCreateRequest.lastName())
                .middleName(clientCreateRequest.middleName())
                .build();

        return clientMapper.map(clientRepository.save(client));
    }

    @Override
    @Metric
    @LogDataSourceError
    @Transactional
    public ClientDto update(ClientUpdateRequest clientUpdateRequest, long id) {
        Client client = getEntityById(id);
        updateClientFields(client, clientUpdateRequest);
        return clientMapper.map(clientRepository.save(client));
    }


    @Override
    @LogDataSourceError
    @Transactional
    public void delete(long id) {
        getEntityById(id);
        clientRepository.deleteByClientId(id);
    }

    @Override
    public boolean existsById(long id) {
        return clientRepository.existsByClientId(id);
    }

    @Transactional(readOnly = true)
    protected Client getEntityById(long id) {
        return clientRepository.findByClientId(id).
                orElseThrow(() -> new ServiceException("Client with id " + id + " not found", HttpStatus.NOT_FOUND));
    }

    private void updateClientFields(Client client, ClientUpdateRequest clientUpdateRequest) {
        Optional.ofNullable(clientUpdateRequest.firstName()).ifPresent(client::setFirstName);
        Optional.ofNullable(clientUpdateRequest.middleName()).ifPresent(client::setMiddleName);
        Optional.ofNullable(clientUpdateRequest.lastName()).ifPresent(client::setLastName);
    }
}

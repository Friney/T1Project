package ru.t1.accountservice.core.service.client;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import ru.t1.accountservice.api.dto.client.ClientCreateRequest;
import ru.t1.accountservice.api.dto.client.ClientDto;
import ru.t1.accountservice.api.dto.client.ClientUpdateRequest;
import ru.t1.accountservice.core.entity.client.Client;
import ru.t1.accountservice.core.entity.client.ClientStatus;
import ru.t1.accountservice.core.exception.ServiceException;
import ru.t1.accountservice.core.mapper.ClientMapper;
import ru.t1.accountservice.core.repository.ClientRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ClientMapper clientMapper;
    @InjectMocks
    private ClientServiceImpl clientService;

    private final long testClientId = 1L;
    private Client testClient;
    private ClientDto testClientDto;

    @BeforeEach
    void setUp() {
        testClient = Client.builder()
                .id(1L)
                .clientId(testClientId)
                .firstName("John")
                .lastName("Doe")
                .middleName("Smith")
                .status(ClientStatus.UNBLOCKED)
                .build();

        testClientDto = ClientDto.builder()
                .id(testClientId)
                .firstName("John")
                .lastName("Doe")
                .middleName("Smith")
                .status(ClientStatus.UNBLOCKED)
                .build();
    }

    @Test
    void getAllSuccess() {
        List<Client> clients = Collections.singletonList(testClient);
        List<ClientDto> expectedDtos = Collections.singletonList(testClientDto);

        when(clientRepository.findAll()).thenReturn(clients);
        when(clientMapper.map(clients)).thenReturn(expectedDtos);

        List<ClientDto> result = clientService.getAll();

        assertEquals(expectedDtos, result);
        verify(clientRepository).findAll();
        verify(clientMapper).map(clients);
    }

    @Test
    void getAllEmptyList() {
        when(clientRepository.findAll()).thenReturn(Collections.emptyList());
        when(clientMapper.map(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<ClientDto> result = clientService.getAll();

        assertTrue(result.isEmpty());
        verify(clientRepository).findAll();
        verify(clientMapper).map(Collections.emptyList());
    }

    @Test
    void getAllByStatusSuccess() {
        List<Client> clients = Collections.singletonList(testClient);
        List<ClientDto> expectedDtos = Collections.singletonList(testClientDto);

        when(clientRepository.findAllByStatus(ClientStatus.UNBLOCKED)).thenReturn(clients);
        when(clientMapper.map(clients)).thenReturn(expectedDtos);

        List<ClientDto> result = clientService.getAllByStatus(ClientStatus.UNBLOCKED);

        assertEquals(expectedDtos, result);
        verify(clientRepository).findAllByStatus(ClientStatus.UNBLOCKED);
        verify(clientMapper).map(clients);
    }

    @Test
    void getAllByStatusEmptyList() {
        when(clientRepository.findAllByStatus(ClientStatus.BLOCKED)).thenReturn(Collections.emptyList());
        when(clientMapper.map(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<ClientDto> result = clientService.getAllByStatus(ClientStatus.BLOCKED);

        assertTrue(result.isEmpty());
        verify(clientRepository).findAllByStatus(ClientStatus.BLOCKED);
        verify(clientMapper).map(Collections.emptyList());
    }

    @Test
    void getByIdSuccess() {
        when(clientRepository.findByClientId(testClientId)).thenReturn(Optional.of(testClient));
        when(clientMapper.map(testClient)).thenReturn(testClientDto);

        ClientDto result = clientService.getById(testClientId);

        assertEquals(testClientDto, result);
        verify(clientRepository).findByClientId(testClientId);
        verify(clientMapper).map(testClient);
    }

    @Test
    void getByIdNotFound() {
        when(clientRepository.findByClientId(testClientId)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> clientService.getById(testClientId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(clientRepository).findByClientId(testClientId);
    }

    @Test
    void getStatusSuccess() {
        when(clientRepository.findByClientId(testClientId)).thenReturn(Optional.of(testClient));

        ClientStatus result = clientService.getStatus(testClientId);

        assertEquals(ClientStatus.UNBLOCKED, result);
        verify(clientRepository).findByClientId(testClientId);
    }

    @Test
    void getStatusNotFound() {
        when(clientRepository.findByClientId(testClientId)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> clientService.getStatus(testClientId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(clientRepository).findByClientId(testClientId);
    }

    @Test
    void createSuccess() {
        ClientCreateRequest createRequest = new ClientCreateRequest(
                "John",
                "Smith",
                "Doe"
        );

        when(clientRepository.getNextClientId()).thenReturn(testClientId);
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        when(clientMapper.map(testClient)).thenReturn(testClientDto);

        ClientDto result = clientService.create(createRequest);

        assertEquals(testClientDto, result);
        verify(clientRepository).getNextClientId();
        verify(clientRepository).save(any(Client.class));
        verify(clientMapper).map(testClient);
    }

    @Test
    void updateSuccess() {
        ClientUpdateRequest updateRequest = new ClientUpdateRequest(
                "Jane",
                "Marie",
                "Smith"
        );

        Client updatedClient = Client.builder()
                .id(1L)
                .clientId(testClientId)
                .firstName("Jane")
                .lastName("Smith")
                .middleName("Marie")
                .status(ClientStatus.UNBLOCKED)
                .build();

        ClientDto updatedClientDto = ClientDto.builder()
                .id(testClientId)
                .firstName("Jane")
                .lastName("Smith")
                .middleName("Marie")
                .status(ClientStatus.UNBLOCKED)
                .build();

        when(clientRepository.findByClientId(testClientId)).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(updatedClient);
        when(clientMapper.map(updatedClient)).thenReturn(updatedClientDto);

        ClientDto result = clientService.update(updateRequest, testClientId);

        assertEquals(updatedClientDto, result);
        verify(clientRepository).findByClientId(testClientId);
        verify(clientRepository).save(any(Client.class));
        verify(clientMapper).map(updatedClient);
    }

    @Test
    void updateNotFound() {
        ClientUpdateRequest updateRequest = new ClientUpdateRequest(
                "Jane",
                "Marie",
                "Smith"
        );

        when(clientRepository.findByClientId(testClientId)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> clientService.update(updateRequest, testClientId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(clientRepository).findByClientId(testClientId);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void updatePartialSuccess() {
        ClientUpdateRequest updateRequest = new ClientUpdateRequest(
                "Jane",
                null,
                null
        );

        Client updatedClient = Client.builder()
                .id(1L)
                .clientId(testClientId)
                .firstName("Jane")
                .lastName("Doe")
                .middleName("Smith")
                .status(ClientStatus.UNBLOCKED)
                .build();

        ClientDto updatedClientDto = ClientDto.builder()
                .id(testClientId)
                .firstName("Jane")
                .lastName("Doe")
                .middleName("Smith")
                .status(ClientStatus.UNBLOCKED)
                .build();

        when(clientRepository.findByClientId(testClientId)).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(updatedClient);
        when(clientMapper.map(updatedClient)).thenReturn(updatedClientDto);

        ClientDto result = clientService.update(updateRequest, testClientId);

        assertEquals(updatedClientDto, result);
        assertEquals("Jane", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals("Smith", result.middleName());
        verify(clientRepository).findByClientId(testClientId);
        verify(clientRepository).save(any(Client.class));
        verify(clientMapper).map(updatedClient);
    }

    @Test
    void updateStatusSuccess() {
        when(clientRepository.findByClientId(testClientId)).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        clientService.updateStatus(testClientId, ClientStatus.BLOCKED);

        verify(clientRepository).findByClientId(testClientId);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void updateStatusNotFound() {
        when(clientRepository.findByClientId(testClientId)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> clientService.updateStatus(testClientId, ClientStatus.BLOCKED));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(clientRepository).findByClientId(testClientId);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void updateStatusSameStatus() {
        when(clientRepository.findByClientId(testClientId)).thenReturn(Optional.of(testClient));

        clientService.updateStatus(testClientId, ClientStatus.UNBLOCKED);

        verify(clientRepository).findByClientId(testClientId);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void deleteSuccess() {
        when(clientRepository.findByClientId(testClientId)).thenReturn(Optional.of(testClient));
        doNothing().when(clientRepository).deleteByClientId(testClientId);

        clientService.delete(testClientId);

        verify(clientRepository).findByClientId(testClientId);
        verify(clientRepository).deleteByClientId(testClientId);
    }

    @Test
    void deleteNotFound() {
        when(clientRepository.findByClientId(testClientId)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> clientService.delete(testClientId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(clientRepository).findByClientId(testClientId);
        verify(clientRepository, never()).deleteByClientId(anyLong());
    }

    @Test
    void existsByIdTrue() {
        when(clientRepository.existsByClientId(testClientId)).thenReturn(true);

        boolean result = clientService.existsById(testClientId);

        assertTrue(result);
        verify(clientRepository).existsByClientId(testClientId);
    }

    @Test
    void existsByIdFalse() {
        when(clientRepository.existsByClientId(testClientId)).thenReturn(false);

        boolean result = clientService.existsById(testClientId);

        assertFalse(result);
        verify(clientRepository).existsByClientId(testClientId);
    }
}

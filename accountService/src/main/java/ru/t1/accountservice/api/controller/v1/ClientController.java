package ru.t1.accountservice.api.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.accountservice.api.Paths;
import ru.t1.accountservice.api.dto.client.ClientCreateRequest;
import ru.t1.accountservice.api.dto.client.ClientDto;
import ru.t1.accountservice.api.dto.client.ClientUpdateRequest;
import ru.t1.accountservice.core.service.client.ClientService;

@RestController
@RequestMapping(Paths.CLIENTS)
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    List<ClientDto> getAll() {
        return clientService.getAll();
    }

    @GetMapping("{id}")
    ClientDto getById(@PathVariable Long id) {
        return clientService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ClientDto create(@Valid @RequestBody ClientCreateRequest clientCreateRequest) {
        return clientService.create(clientCreateRequest);
    }

    @PatchMapping("{id}")
    ClientDto update(@Valid @RequestBody ClientUpdateRequest clientUpdateRequest, @PathVariable Long id) {
        return clientService.update(clientUpdateRequest, id);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        clientService.delete(id);
    }
}

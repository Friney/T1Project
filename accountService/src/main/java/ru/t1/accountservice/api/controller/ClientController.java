package ru.t1.accountservice.api.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    ClientDto create(@RequestBody ClientCreateRequest clientCreateRequest) {
        return clientService.create(clientCreateRequest);
    }

    @PatchMapping("{id}")
    ClientDto update(@PathVariable Long id, @RequestBody ClientUpdateRequest clientUpdateRequest) {
        return clientService.update(id, clientUpdateRequest);
    }

    @DeleteMapping("{id}")
    void delete(@PathVariable Long id) {
        clientService.delete(id);
    }
}

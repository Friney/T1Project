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
import ru.t1.accountservice.api.dto.account.AccountCreateRequest;
import ru.t1.accountservice.api.dto.account.AccountDto;
import ru.t1.accountservice.api.dto.account.AccountUpdateRequest;
import ru.t1.accountservice.core.service.account.AccountService;

@RestController
@RequestMapping(Paths.ACCOUNTS)
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    List<AccountDto> getAll(@PathVariable Long clientId) {
        return accountService.getAll(clientId);
    }

    @GetMapping("{id}")
    AccountDto getById(@PathVariable Long id, @PathVariable Long clientId) {
        return accountService.getById(id, clientId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AccountDto create(@Valid @RequestBody AccountCreateRequest accountCreateRequest, @PathVariable Long clientId) {
        return accountService.create(accountCreateRequest, clientId);
    }

    @PatchMapping("{id}")
    AccountDto update(@Valid @RequestBody AccountUpdateRequest accountUpdateRequest, @PathVariable Long id, @PathVariable Long clientId) {
        return accountService.update(accountUpdateRequest, id, clientId);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id, @PathVariable Long clientId) {
        accountService.delete(id, clientId);
    }
}

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
import ru.t1.accountservice.api.dto.transaction.TransactionCreateRequest;
import ru.t1.accountservice.api.dto.transaction.TransactionDto;
import ru.t1.accountservice.api.dto.transaction.TransactionUpdateRequest;
import ru.t1.accountservice.core.service.transaction.TransactionService;

@RestController
@RequestMapping(Paths.TRANSACTIONS)
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    List<TransactionDto> getAll(@PathVariable Long accountId) {
        return transactionService.getAll(accountId);
    }

    @GetMapping("{id}")
    TransactionDto getById(@PathVariable Long id, @PathVariable Long accountId) {
        return transactionService.getById(id, accountId);
    }

    @PostMapping
    TransactionDto create(@RequestBody TransactionCreateRequest transactionCreateRequest, @PathVariable Long accountId) {
        return transactionService.create(transactionCreateRequest, accountId);
    }

    @PatchMapping("{id}")
    TransactionDto update(@RequestBody TransactionUpdateRequest transactionUpdateRequest, @PathVariable Long id, @PathVariable Long accountId) {
        return transactionService.update(transactionUpdateRequest, id, accountId);
    }

    @DeleteMapping("{id}")
    void delete(@PathVariable Long id, @PathVariable Long accountId) {
        transactionService.delete(id, accountId);
    }
}

package ru.t1.accountservice.core.service.generator;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.t1.accountservice.api.dto.account.AccountCreateRequest;
import ru.t1.accountservice.api.dto.account.AccountDto;
import ru.t1.accountservice.api.dto.client.ClientCreateRequest;
import ru.t1.accountservice.api.dto.client.ClientDto;
import ru.t1.accountservice.api.dto.transaction.TransactionCreateRequest;
import ru.t1.accountservice.core.entity.account.AccountType;
import ru.t1.accountservice.core.service.account.AccountService;
import ru.t1.accountservice.core.service.client.ClientService;
import ru.t1.accountservice.core.service.transaction.TransactionService;

@Service
@RequiredArgsConstructor
public class DataGeneratorService {

    private final ClientService clientService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final Faker faker = new Faker();

    @EventListener(ApplicationReadyEvent.class)
    public void generateDataIfNeeded() {
        List<ClientDto> clients = clientService.getAll();
        List<AccountDto> accounts = getAllAccountsForClients(clients);

        generateClientsIfNeeded(clients);
        generateAccountsIfNeeded(clients, accounts);
        generateTransactionsIfNeeded(accounts);
    }

    private List<AccountDto> getAllAccountsForClients(List<ClientDto> clients) {
        List<AccountDto> allAccounts = new ArrayList<>();
        for (ClientDto client : clients) {
            allAccounts.addAll(accountService.getAll(client.id()));
        }
        return allAccounts;
    }

    private void generateClientsIfNeeded(List<ClientDto> clients) {
        int minClients = 10;
        int clientsToGenerate = minClients - clients.size();
        for (int i = 0; i < clientsToGenerate; i++) {
            ClientCreateRequest request = new ClientCreateRequest(
                    faker.name().firstName(),
                    faker.name().lastName(),
                    faker.name().firstName()
            );
            ClientDto createdClient = clientService.create(request);
            clients.add(createdClient);
        }
    }

    private void generateAccountsIfNeeded(List<ClientDto> clients, List<AccountDto> accounts) {
        int minAccounts = 10;
        AccountType[] accountTypes = AccountType.values();
        int accountsToGenerate = minAccounts - accounts.size();
        for (int i = 0; i < accountsToGenerate; i++) {
            ClientDto randomClient = clients.get(faker.random().nextInt(clients.size()));
            AccountType randomType = accountTypes[faker.random().nextInt(accountTypes.length)];
            AccountCreateRequest request = new AccountCreateRequest(
                    randomType,
                    BigDecimal.valueOf(faker.number().randomDouble(2, 0, 100000))
            );
            AccountDto createdAccount = accountService.create(request, randomClient.id());
            accounts.add(createdAccount);
        }
    }


    private void generateTransactionsIfNeeded(List<AccountDto> accounts) {
        int minTransactions = 10;
        int transactionCount = 0;
        for (AccountDto account : accounts) {
            transactionCount += transactionService.getAll(account.id()).size();
            if (transactionCount >= minTransactions) {
                break;
            }
        }

        int transactionsToGenerate = minTransactions - transactionCount;
        for (int i = 0; i < transactionsToGenerate; i++) {
            if (!accounts.isEmpty()) {
                AccountDto randomAccount = accounts.get(faker.random().nextInt(accounts.size()));
                TransactionCreateRequest request = new TransactionCreateRequest(
                        BigDecimal.valueOf(faker.number().randomDouble(2, -10000, 10000)),
                        faker.timeAndDate().past(30, TimeUnit.DAYS)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                );
                transactionService.create(request, randomAccount.id());
            }
        }
    }
}

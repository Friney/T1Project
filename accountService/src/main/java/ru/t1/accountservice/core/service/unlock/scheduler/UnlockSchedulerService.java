package ru.t1.accountservice.core.service.unlock.scheduler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.t1.accountservice.api.dto.account.AccountDto;
import ru.t1.accountservice.api.dto.client.ClientDto;
import ru.t1.accountservice.api.dto.unlock.UnlockDecision;
import ru.t1.accountservice.core.entity.account.AccountStatus;
import ru.t1.accountservice.core.entity.client.ClientStatus;
import ru.t1.accountservice.core.service.account.AccountService;
import ru.t1.accountservice.core.service.client.ClientService;
import ru.t1.accountservice.core.service.unlock.request.UnlockRequestService;

@Service
@Slf4j
@RequiredArgsConstructor
public class UnlockSchedulerService {

    private final UnlockRequestService unlockRequestService;
    private final ClientService clientService;
    private final AccountService accountService;

    @Value("${t1.unlock.scheduling.clients-count}")
    private int clientsCountOnUnlock;
    @Value("${t1.unlock.scheduling.accounts-count}")
    private int accountsCountOnUnlock;

    @Scheduled(fixedDelayString = "${t1.unlock.scheduling.interval}")
    public void processBlockedClients() {
        List<ClientDto> clients = clientService.getAllByStatus(ClientStatus.BLOCKED);
        for (int i = 0; i < clientsCountOnUnlock && i < clients.size(); i++) {
            UnlockDecision decision = unlockRequestService.requestUnlockClient(clients.get(i).id());
            if (decision == UnlockDecision.UNLOCKED) {
                clientService.updateStatus(clients.get(i).id(), ClientStatus.UNBLOCKED);
            }
        }
    }

    @Scheduled(fixedDelayString = "${t1.unlock.scheduling.interval}")
    public void processArrestedAccounts() {
        List<AccountDto> accounts = accountService.getAllByStatus(AccountStatus.ARRESTED);
        for (int i = 0; i < accountsCountOnUnlock && i < accounts.size(); i++) {
            UnlockDecision decision = unlockRequestService.requestUnlockAccount(accounts.get(i).id());
            if (decision == UnlockDecision.UNLOCKED) {
                accountService.updateStatus(accounts.get(i).id(), AccountStatus.OPEN);
            }
        }
    }
}
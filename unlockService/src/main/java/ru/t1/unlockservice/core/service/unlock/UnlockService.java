package ru.t1.unlockservice.core.service.unlock;


import ru.t1.unlockservice.api.dto.unlock.UnlockDecision;

public interface UnlockService {

    UnlockDecision requestUnlockClient(Long clientId);

    UnlockDecision requestUnlockAccount(Long accountId);
}

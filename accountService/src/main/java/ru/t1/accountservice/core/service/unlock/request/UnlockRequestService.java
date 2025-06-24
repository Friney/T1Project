package ru.t1.accountservice.core.service.unlock.request;

import ru.t1.accountservice.api.dto.unlock.UnlockDecision;

public interface UnlockRequestService {

    UnlockDecision requestUnlockClient(Long clientId);

    UnlockDecision requestUnlockAccount(Long accountId);
}

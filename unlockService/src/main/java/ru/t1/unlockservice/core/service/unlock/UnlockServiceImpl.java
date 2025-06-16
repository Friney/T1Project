package ru.t1.unlockservice.core.service.unlock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.t1.unlockservice.api.dto.unlock.UnlockDecision;

@Slf4j
@Service
public class UnlockServiceImpl implements UnlockService {

    @Override
    public UnlockDecision requestUnlockClient(Long clientId) {
        UnlockDecision decision = Math.random() < 0.6 ? UnlockDecision.REJECTED : UnlockDecision.UNLOCKED;
        log.info("Unlock decision for client {} is {}", clientId, decision);
        return decision;
    }

    @Override
    public UnlockDecision requestUnlockAccount(Long accountId) {
        UnlockDecision decision = Math.random() < 0.6 ? UnlockDecision.REJECTED : UnlockDecision.UNLOCKED;
        log.info("Unlock decision for account {} is {}", accountId, decision);
        return decision;
    }
}

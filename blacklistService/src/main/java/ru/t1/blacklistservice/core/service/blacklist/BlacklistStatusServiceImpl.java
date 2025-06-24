package ru.t1.blacklistservice.core.service.blacklist;

import java.security.SecureRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.t1.blacklistservice.api.dto.blacklist.ClientBlacklistStatus;

@Slf4j
@Service
public class BlacklistStatusServiceImpl implements BlacklistStatusService {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public ClientBlacklistStatus getBlacklistStatus(Long clientId, Long accountId) {
        ClientBlacklistStatus status = secureRandom.nextInt(10) < 1 ? ClientBlacklistStatus.BLACKLISTED : ClientBlacklistStatus.NOT_BLACKLISTED;
        log.info("Blacklist status for client {} and account {} is {}", clientId, accountId, status);
        return status;
    }
}

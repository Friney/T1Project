package ru.t1.blacklistservice.core.service.blacklist;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.t1.blacklistservice.api.dto.blacklist.ClientBlacklistStatus;

@Slf4j
@Service
public class BlacklistStatusServiceImpl implements BlacklistStatusService {

    @Override
    public ClientBlacklistStatus getBlacklistStatus(Long clientId, Long accountId) {
        ClientBlacklistStatus status = Math.random() < 0.1 ? ClientBlacklistStatus.BLACKLISTED : ClientBlacklistStatus.NOT_BLACKLISTED;
        log.info("Blacklist status for client {} and account {} is {}", clientId, accountId, status);
        return status;
    }
}

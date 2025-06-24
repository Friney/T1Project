package ru.t1.blacklistservice.core.service.blacklist;

import ru.t1.blacklistservice.api.dto.blacklist.ClientBlacklistStatus;

public interface BlacklistStatusService {

    ClientBlacklistStatus getBlacklistStatus(Long clientId, Long accountId);
}

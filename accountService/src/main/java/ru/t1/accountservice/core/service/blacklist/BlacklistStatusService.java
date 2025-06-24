package ru.t1.accountservice.core.service.blacklist;

import ru.t1.accountservice.api.dto.blacklist.ClientBlacklistStatus;

public interface BlacklistStatusService {

    ClientBlacklistStatus getBlacklistStatus(Long clientId, Long accountId);
}

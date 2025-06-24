package ru.t1.blacklistservice.api.controller.v1.blacklist;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.blacklistservice.api.dto.blacklist.ClientBlacklistStatus;
import ru.t1.blacklistservice.core.service.blacklist.BlacklistStatusService;

@RestController
@RequestMapping("api/v1/client/{clientId}/account/{accountId}/blacklist-status")
@RequiredArgsConstructor
public class BlacklistStatusController {

    private final BlacklistStatusService blacklistStatusService;

    @GetMapping
    public ClientBlacklistStatus getBlacklistStatus(@PathVariable Long clientId, @PathVariable Long accountId) {
        return blacklistStatusService.getBlacklistStatus(clientId, accountId);
    }
}

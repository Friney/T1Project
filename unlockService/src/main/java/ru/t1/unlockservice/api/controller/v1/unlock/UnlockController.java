package ru.t1.unlockservice.api.controller.v1.unlock;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.unlockservice.api.dto.unlock.UnlockDecision;
import ru.t1.unlockservice.core.service.unlock.UnlockService;

@RestController
@RequestMapping("api/v1/check-request")
@RequiredArgsConstructor
public class UnlockController {

    private final UnlockService unlockService;

    @PostMapping("/clients/{id}")
    public UnlockDecision unlockClient(@PathVariable long id) {
        return unlockService.requestUnlockClient(id);
    }

    @PostMapping("/accounts/{id}")
    public UnlockDecision unlockAccount(@PathVariable long id) {
        return unlockService.requestUnlockAccount(id);
    }
}

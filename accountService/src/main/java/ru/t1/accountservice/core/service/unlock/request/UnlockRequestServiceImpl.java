package ru.t1.accountservice.core.service.unlock.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.t1.accountservice.api.dto.unlock.UnlockDecision;
import ru.t1.accountservice.core.exception.ServiceException;

@Service
@Slf4j
@RequiredArgsConstructor
public class UnlockRequestServiceImpl implements UnlockRequestService {

    private final WebClient unlockWebClient;
    @Value("${t1.unlock.endpoints.request-client}")
    private String urlRequestClient;
    @Value("${t1.unlock.endpoints.request-account}")
    private String urlRequestAccount;

    @Override
    public UnlockDecision requestUnlockClient(Long clientId) {
        return performUnlockRequest(urlRequestClient, clientId);
    }

    @Override
    public UnlockDecision requestUnlockAccount(Long accountId) {
        return performUnlockRequest(urlRequestAccount, accountId);
    }

    private UnlockDecision performUnlockRequest(String url, Long id) {
        return unlockWebClient
                .post()
                .uri(url, id)
                .retrieve()
                .bodyToMono(UnlockDecision.class)
                .doOnError(error -> {
                    log.error("Error while getting unlock decision from service");
                    throw new ServiceException(error.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                })
                .block();
    }
}

package ru.t1.accountservice.core.service.blacklist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.t1.accountservice.api.dto.blacklist.ClientBlacklistStatus;
import ru.t1.accountservice.core.exception.ServiceException;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlacklistStatusServiceImpl implements BlacklistStatusService {

    private final WebClient blacklistWebClient;
    @Value("${t1.blacklist.url-get-status}")
    private String url;


    @Override
    public ClientBlacklistStatus getBlacklistStatus(Long clientId, Long accountId) {
        return blacklistWebClient
                .get()
                .uri(url, clientId, accountId)
                .retrieve()
                .bodyToMono(ClientBlacklistStatus.class)
                .doOnError(error -> {
                            log.error("Error while getting blacklist status from service");
                            throw new ServiceException(error.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                )
                .block();
    }
}

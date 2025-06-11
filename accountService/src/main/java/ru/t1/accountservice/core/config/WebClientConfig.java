package ru.t1.accountservice.core.config;

import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${t1.blacklist.base-url}")
    private String blacklistBaseUrl;
    @Value("${t1.blacklist.username}")
    private String blacklistUsername;
    @Value("${t1.blacklist.password}")
    private String blacklistPassword;

    @Bean
    public WebClient blacklistWebClient() {
        String auth = blacklistUsername + ":" + blacklistPassword;
        String encoded = Base64.getEncoder().encodeToString(auth.getBytes());
        return WebClient.builder()
                .baseUrl(blacklistBaseUrl)
                .defaultHeader("Authorization", "Basic " + encoded)
                .build();
    }
}

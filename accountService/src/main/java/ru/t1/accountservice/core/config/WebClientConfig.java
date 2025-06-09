package ru.t1.accountservice.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${t1.blacklist.base-url}")
    private String blacklistBaseUrl;

    @Bean
    public WebClient blacklistWebClient() {
        return WebClient.create(blacklistBaseUrl);
    }
}

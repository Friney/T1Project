package ru.t1.accountservice.core.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.t1.accountservice.core.entity.account.AccountStatus;
import ru.t1.accountservice.core.entity.client.ClientStatus;
import ru.t1.accountservice.core.service.account.AccountService;
import ru.t1.accountservice.core.service.client.ClientService;

@Component
@RequiredArgsConstructor
public class MetricsProvider {
    private final ClientService clientService;
    private final AccountService accountService;

    @Bean
    public MeterBinder meterBinder() {
        return registry -> {
            Gauge.builder("service.blocked.clients.count", clientService,
                            service -> service.getAllByStatus(ClientStatus.BLOCKED).size())
                    .description("Number of blocked clients")
                    .register(registry);

            Gauge.builder("service.arrested.accounts.count", accountService,
                            service -> service.getAllByStatus(AccountStatus.ARRESTED).size())
                    .description("Number of arrested accounts")
                    .register(registry);
        };
    }
}

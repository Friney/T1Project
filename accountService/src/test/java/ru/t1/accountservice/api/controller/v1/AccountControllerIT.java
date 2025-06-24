package ru.t1.accountservice.api.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.t1.accountservice.api.Paths;
import ru.t1.accountservice.api.dto.account.AccountCreateRequest;
import ru.t1.accountservice.api.dto.account.AccountDto;
import ru.t1.accountservice.api.dto.account.AccountUpdateRequest;
import ru.t1.accountservice.api.dto.client.ClientCreateRequest;
import ru.t1.accountservice.api.dto.client.ClientDto;
import ru.t1.accountservice.api.dto.jwt.JwtAuthenticationDto;
import ru.t1.accountservice.api.dto.user.LoginRequest;
import ru.t1.accountservice.api.dto.user.UserRegisterRequest;
import ru.t1.accountservice.core.entity.account.AccountStatus;
import ru.t1.accountservice.core.entity.account.AccountType;
import ru.t1.accountservice.core.service.account.AccountService;
import ru.t1.accountservice.core.service.auth.AuthService;
import ru.t1.accountservice.core.service.client.ClientService;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class AccountControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.2");

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private AuthService authService;

    private String getBaseUrl() {
        return Paths.CLIENTS_V1 + "/" + clientId + "/accounts";
    }

    private Long clientId = 1L;
    private Long accountId = 1L;
    private String jwtToken;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        ClientCreateRequest clientCreateRequest = new ClientCreateRequest(
                "John",
                "Doe",
                "John"
        );
        ClientDto savedClient = clientService.create(clientCreateRequest);
        clientId = savedClient.id();

        createTestUserAndGetToken();
    }

    @Test
    void getAllSuccess() throws Exception {
        createTestAccount();

        mockMvc.perform(get(getBaseUrl())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(accountId))
                .andExpect(jsonPath("$[0].accountType").value(AccountType.DEBIT.name()))
                .andExpect(jsonPath("$[0].balance").value(1000))
                .andExpect(jsonPath("$[0].status").value(AccountStatus.OPEN.name()));
    }

    @Test
    void getAllEmptyList() throws Exception {
        mockMvc.perform(get(getBaseUrl())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getByIdSuccess() throws Exception {
        createTestAccount();

        mockMvc.perform(get(getBaseUrl() + "/" + accountId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId))
                .andExpect(jsonPath("$.accountType").value(AccountType.DEBIT.name()))
                .andExpect(jsonPath("$.balance").value(1000))
                .andExpect(jsonPath("$.status").value(AccountStatus.OPEN.name()));
    }

    @Test
    void getByIdNotFound() throws Exception {
        mockMvc.perform(get(getBaseUrl() + "/999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSuccess() throws Exception {
        AccountCreateRequest request = new AccountCreateRequest(
                AccountType.DEBIT,
                BigDecimal.valueOf(1000)
        );

        ResultActions result = mockMvc.perform(post(getBaseUrl())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountType").value(AccountType.DEBIT.name()))
                .andExpect(jsonPath("$.balance").value(1000))
                .andExpect(jsonPath("$.status").value(AccountStatus.OPEN.name()));

        String response = result.andReturn().getResponse().getContentAsString();
        AccountDto createdAccount = objectMapper.readValue(response, AccountDto.class);
        assertNotNull(createdAccount.id());
        assertEquals(BigDecimal.valueOf(1000), createdAccount.balance());
    }

    @Test
    void createClientNotFound() throws Exception {
        AccountCreateRequest request = new AccountCreateRequest(
                AccountType.DEBIT,
                BigDecimal.valueOf(1000)
        );

        mockMvc.perform(post(Paths.CLIENTS_V1 + "/999/accounts")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSuccess() throws Exception {
        createTestAccount();

        AccountUpdateRequest request = new AccountUpdateRequest(
                AccountType.CREDIT,
                BigDecimal.valueOf(2000)
        );

        mockMvc.perform(patch(getBaseUrl() + "/" + accountId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountType").value(AccountType.CREDIT.name()))
                .andExpect(jsonPath("$.balance").value(2000));

        var updatedAccount = accountService.getOnlyById(accountId);
        assertEquals(AccountType.CREDIT, updatedAccount.accountType());
        assertEquals(0, BigDecimal.valueOf(2000).compareTo(updatedAccount.balance()));
    }

    @Test
    void updateNotFound() throws Exception {
        AccountUpdateRequest request = new AccountUpdateRequest(
                AccountType.CREDIT,
                BigDecimal.valueOf(2000)
        );

        mockMvc.perform(patch(getBaseUrl() + "/999")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSuccess() throws Exception {
        createTestAccount();

        mockMvc.perform(delete(getBaseUrl() + "/" + accountId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        assertFalse(accountService.existsById(accountId));
    }

    @Test
    void deleteNotFound() throws Exception {
        mockMvc.perform(delete(getBaseUrl() + "/999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void unauthorizedRequest() throws Exception {
        mockMvc.perform(get(getBaseUrl()))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidToken() throws Exception {
        mockMvc.perform(get(getBaseUrl())
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isForbidden());
    }

    private void createTestAccount() {
        AccountDto account = accountService.create(AccountCreateRequest.builder()
                .accountType(AccountType.DEBIT)
                .balance(BigDecimal.valueOf(1000))
                .build(), clientId);
        accountId = account.id();
    }

    private void createTestUserAndGetToken() {
        String testUser = "testUser";
        String testPassword = "testPassword";
        UserRegisterRequest registerRequest = new UserRegisterRequest(
                testUser,
                testPassword,
                testPassword
        );
        authService.registration(registerRequest);

        LoginRequest loginRequest = new LoginRequest(testUser, testPassword);
        JwtAuthenticationDto authDto = authService.login(loginRequest);
        jwtToken = authDto.token();
    }
}

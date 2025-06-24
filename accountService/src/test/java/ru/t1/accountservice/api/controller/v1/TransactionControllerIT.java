package ru.t1.accountservice.api.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import ru.t1.accountservice.api.dto.client.ClientCreateRequest;
import ru.t1.accountservice.api.dto.client.ClientDto;
import ru.t1.accountservice.api.dto.jwt.JwtAuthenticationDto;
import ru.t1.accountservice.api.dto.transaction.TransactionCreateRequest;
import ru.t1.accountservice.api.dto.transaction.TransactionDto;
import ru.t1.accountservice.api.dto.transaction.TransactionUpdateRequest;
import ru.t1.accountservice.api.dto.user.LoginRequest;
import ru.t1.accountservice.api.dto.user.UserRegisterRequest;
import ru.t1.accountservice.core.entity.account.AccountType;
import ru.t1.accountservice.core.entity.transaction.TransactionStatus;
import ru.t1.accountservice.core.service.account.AccountService;
import ru.t1.accountservice.core.service.auth.AuthService;
import ru.t1.accountservice.core.service.client.ClientService;
import ru.t1.accountservice.core.service.transaction.TransactionService;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
class TransactionControllerIT {

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
    @Autowired
    private TransactionService transactionService;

    private final String baseUrl = Paths.ACCOUNTS_V1_WITHOUT_CLIENT;
    private Long clientId;
    private Long accountId;
    private Long transactionId;
    private String jwtToken;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        // Создаем тестового клиента
        ClientCreateRequest clientCreateRequest = new ClientCreateRequest(
                "Test",
                "User",
                "TestUser"
        );
        ClientDto savedClient = clientService.create(clientCreateRequest);
        clientId = savedClient.id();

        // Создаем тестовый аккаунт
        AccountCreateRequest accountCreateRequest = new AccountCreateRequest(
                AccountType.DEBIT,
                BigDecimal.valueOf(1000)
        );
        AccountDto savedAccount = accountService.create(accountCreateRequest, clientId);
        accountId = savedAccount.id();

        // Создаем пользователя и получаем токен
        createTestUserAndGetToken();
    }

    @Test
    void getAllSuccess() throws Exception {
        // Создаем тестовую транзакцию
        createTestTransaction();

        // Выполняем запрос
        mockMvc.perform(get(getTransactionUrl())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(transactionId))
                .andExpect(jsonPath("$[0].amount").value(100))
                .andExpect(jsonPath("$[0].status").value(TransactionStatus.ACCEPTED.name()));
    }

    @Test
    void getAllEmptyList() throws Exception {
        mockMvc.perform(get(getTransactionUrl())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getByIdSuccess() throws Exception {
        createTestTransaction();

        mockMvc.perform(get(getTransactionUrl() + "/" + transactionId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.status").value(TransactionStatus.ACCEPTED.name()));
    }

    @Test
    void getByIdNotFound() throws Exception {
        mockMvc.perform(get(getTransactionUrl() + "/999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSuccess() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest(
                BigDecimal.valueOf(100),
                LocalDateTime.now()
        );

        ResultActions result = mockMvc.perform(post(getTransactionUrl())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.status").value(TransactionStatus.ACCEPTED.name()));

        String response = result.andReturn().getResponse().getContentAsString();
        TransactionDto createdTransaction = objectMapper.readValue(response, TransactionDto.class);
        assertNotNull(createdTransaction.id());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(createdTransaction.amount()));
    }

    @Test
    void createAccountNotFound() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest(
                BigDecimal.valueOf(100),
                LocalDateTime.now()
        );

        accountId += 100;

        mockMvc.perform(post(getTransactionUrl())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSuccess() throws Exception {
        createTestTransaction();

        TransactionUpdateRequest request = new TransactionUpdateRequest(
                BigDecimal.valueOf(200),
                LocalDateTime.now()
        );

        mockMvc.perform(patch(getTransactionUrl() + "/" + transactionId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(200));

        TransactionDto updatedTransaction = transactionService.getById(transactionId, accountId);
        assertEquals(0, BigDecimal.valueOf(200).compareTo(updatedTransaction.amount()));
    }

    @Test
    void updateNotFound() throws Exception {
        TransactionUpdateRequest request = new TransactionUpdateRequest(
                BigDecimal.valueOf(200),
                LocalDateTime.now()
        );

        mockMvc.perform(patch(getTransactionUrl() + "/999")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSuccess() throws Exception {
        createTestTransaction();

        mockMvc.perform(delete(getTransactionUrl() + "/" + transactionId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        // Проверяем, что транзакция удалена
        mockMvc.perform(get(getTransactionUrl() + "/" + transactionId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteNotFound() throws Exception {
        mockMvc.perform(delete(getTransactionUrl() + "/999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void unauthorizedRequest() throws Exception {
        mockMvc.perform(get(getTransactionUrl()))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidToken() throws Exception {
        mockMvc.perform(get(getTransactionUrl())
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isForbidden());
    }

    private String getTransactionUrl() {
        return baseUrl + "/" + accountId + "/transactions";
    }

    private void createTestTransaction() {
        TransactionCreateRequest request = new TransactionCreateRequest(
                BigDecimal.valueOf(100),
                LocalDateTime.now()
        );
        TransactionDto transaction = transactionService.create(request, accountId);
        transactionId = transaction.id();
    }

    private void createTestUserAndGetToken() {
        UserRegisterRequest registerRequest = new UserRegisterRequest(
                "testUser",
                "testPassword",
                "testPassword"
        );
        authService.registration(registerRequest);

        LoginRequest loginRequest = new LoginRequest("testUser", "testPassword");
        JwtAuthenticationDto authDto = authService.login(loginRequest);
        jwtToken = authDto.token();
    }
}

package ru.t1.accountservice.api.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
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
import ru.t1.accountservice.api.dto.client.ClientCreateRequest;
import ru.t1.accountservice.api.dto.client.ClientDto;
import ru.t1.accountservice.api.dto.client.ClientUpdateRequest;
import ru.t1.accountservice.api.dto.jwt.JwtAuthenticationDto;
import ru.t1.accountservice.api.dto.user.LoginRequest;
import ru.t1.accountservice.api.dto.user.UserRegisterRequest;
import ru.t1.accountservice.core.service.auth.AuthService;
import ru.t1.accountservice.core.service.client.ClientService;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
class ClientControllerIT {

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
    private ClientService clientService;
    @Autowired
    private AuthService authService;

    private static final String BASE_URL = Paths.CLIENTS_V1;
    private Long clientId;
    private String jwtToken;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        createTestUserAndGetToken();
    }

    @Test
    void getAllSuccess() throws Exception {
        createTestClient();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.firstName == 'Test' && @.lastName == 'User' && @.id == %d)]", clientId).exists());
    }

    @Test
    void getByIdSuccess() throws Exception {
        createTestClient();

        mockMvc.perform(get(BASE_URL + "/" + clientId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    void getByIdNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSuccess() throws Exception {
        ClientCreateRequest request = new ClientCreateRequest(
                "Test",
                "Middle",
                "User"
        );

        ResultActions result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.middleName").value("Middle"))
                .andExpect(jsonPath("$.lastName").value("User"));

        String response = result.andReturn().getResponse().getContentAsString();
        ClientDto createdClient = objectMapper.readValue(response, ClientDto.class);
        assertNotNull(createdClient.id());
        assertTrue(clientService.existsById(createdClient.id()));
    }

    @Test
    void createInvalidRequest() throws Exception {
        ClientCreateRequest request = new ClientCreateRequest(
                "",
                "Middle",
                ""
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSuccess() throws Exception {
        createTestClient();

        ClientUpdateRequest request = new ClientUpdateRequest(
                "UpdatedTest",
                "UpdatedMiddle",
                "UpdatedUser"
        );

        mockMvc.perform(patch(BASE_URL + "/" + clientId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedTest"))
                .andExpect(jsonPath("$.middleName").value("UpdatedMiddle"))
                .andExpect(jsonPath("$.lastName").value("UpdatedUser"));

        ClientDto updatedClient = clientService.getById(clientId);
        assertEquals("UpdatedTest", updatedClient.firstName());
        assertEquals("UpdatedMiddle", updatedClient.middleName());
        assertEquals("UpdatedUser", updatedClient.lastName());
    }

    @Test
    void updateNotFound() throws Exception {
        ClientUpdateRequest request = new ClientUpdateRequest(
                "UpdatedTest",
                "UpdatedMiddle",
                "UpdatedUser"
        );

        mockMvc.perform(patch(BASE_URL + "/999")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePartialSuccess() throws Exception {
        createTestClient();

        ClientUpdateRequest request = new ClientUpdateRequest(
                "UpdatedTest",
                null,
                null
        );

        mockMvc.perform(patch(BASE_URL + "/" + clientId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedTest"))
                .andExpect(jsonPath("$.lastName").value("User"));

        ClientDto updatedClient = clientService.getById(clientId);
        assertEquals("UpdatedTest", updatedClient.firstName());
        assertEquals("User", updatedClient.lastName());
    }

    @Test
    void deleteSuccess() throws Exception {
        createTestClient();

        mockMvc.perform(delete(BASE_URL + "/" + clientId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        assertFalse(clientService.existsById(clientId));
    }

    @Test
    void deleteNotFound() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void unauthorizedRequest() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidToken() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + "invalid_token"))
                .andExpect(status().isForbidden());
    }

    private void createTestClient() {
        ClientCreateRequest request = new ClientCreateRequest(
                "Test",
                null,
                "User"
        );
        ClientDto savedClient = clientService.create(request);
        clientId = savedClient.id();
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

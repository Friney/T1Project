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
import ru.t1.accountservice.api.dto.jwt.JwtAuthenticationDto;
import ru.t1.accountservice.api.dto.jwt.RefreshTokenDto;
import ru.t1.accountservice.api.dto.user.LoginRequest;
import ru.t1.accountservice.api.dto.user.UserChangePasswordRequest;
import ru.t1.accountservice.api.dto.user.UserDto;
import ru.t1.accountservice.api.dto.user.UserRegisterRequest;
import ru.t1.accountservice.api.dto.user.UserUpdateRequest;
import ru.t1.accountservice.core.service.auth.AuthService;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class AuthControllerIT {

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
    private AuthService authService;

    private final String baseUrl = Paths.AUTH_V1;
    private final String testUser = "testUser";
    private final String testPassword = "testPassword";
    private String jwtToken;
    private String refreshToken;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        createTestUser();
    }

    private void createTestUser() {
        UserRegisterRequest registerRequest = new UserRegisterRequest(
                testUser,
                testPassword,
                testPassword
        );
        authService.registration(registerRequest);

        LoginRequest loginRequest = new LoginRequest(testUser, testPassword);
        JwtAuthenticationDto authDto = authService.login(loginRequest);
        jwtToken = authDto.token();
        refreshToken = authDto.refreshToken();
    }

    @Test
    void loginSuccess() throws Exception {
        LoginRequest request = new LoginRequest(testUser, testPassword);

        mockMvc.perform(post(baseUrl + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void loginInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest(testUser, "wrongPassword");

        mockMvc.perform(post(baseUrl + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginUserNotFound() throws Exception {
        LoginRequest request = new LoginRequest("nonExistentUser", testPassword);

        mockMvc.perform(post(baseUrl + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationSuccess() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest(
                "newUser",
                "newPassword",
                "newPassword"
        );

        ResultActions result = mockMvc.perform(post(baseUrl + "/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("newUser"));

        String response = result.andReturn().getResponse().getContentAsString();
        UserDto createdUser = objectMapper.readValue(response, UserDto.class);
        assertNotNull(createdUser.id());
    }

    @Test
    void registrationPasswordMismatch() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest(
                "newUser",
                "password1",
                "password2"
        );

        mockMvc.perform(post(baseUrl + "/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrationUserExists() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest(
                testUser,
                testPassword,
                testPassword
        );

        mockMvc.perform(post(baseUrl + "/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshTokenSuccess() throws Exception {
        RefreshTokenDto request = new RefreshTokenDto(refreshToken);

        mockMvc.perform(post(baseUrl + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").value(refreshToken));
    }

    @Test
    void refreshTokenInvalid() throws Exception {
        RefreshTokenDto request = new RefreshTokenDto("invalid_token");

        mockMvc.perform(post(baseUrl + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePasswordSuccess() throws Exception {
        UserChangePasswordRequest request = new UserChangePasswordRequest(
                testPassword,
                "newPassword",
                "newPassword"
        );

        mockMvc.perform(patch(baseUrl + "/change-password")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        LoginRequest loginRequest = new LoginRequest(testUser, "newPassword");
        mockMvc.perform(post(baseUrl + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void changePasswordOldPasswordIncorrect() throws Exception {
        UserChangePasswordRequest request = new UserChangePasswordRequest(
                "wrongPassword",
                "newPassword",
                "newPassword"
        );

        mockMvc.perform(patch(baseUrl + "/change-password")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePasswordNewPasswordMismatch() throws Exception {
        UserChangePasswordRequest request = new UserChangePasswordRequest(
                testPassword,
                "newPassword1",
                "newPassword2"
        );

        mockMvc.perform(patch(baseUrl + "/change-password")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserSuccess() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest("newUsername");

        mockMvc.perform(patch(baseUrl)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("newUsername"));
    }

    @Test
    void updateUserLoginExists() throws Exception { //!
        UserRegisterRequest registerRequest = new UserRegisterRequest(
                "anotherUser",
                testPassword,
                testPassword
        );
        authService.registration(registerRequest);

        UserUpdateRequest request = new UserUpdateRequest("anotherUser");

        mockMvc.perform(patch(baseUrl)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUserSuccess() throws Exception {
        mockMvc.perform(delete(baseUrl)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        LoginRequest loginRequest = new LoginRequest(testUser, testPassword);
        mockMvc.perform(post(baseUrl + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthorizedRequest() throws Exception {
        mockMvc.perform(delete(baseUrl))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidToken() throws Exception {
        mockMvc.perform(delete(baseUrl)
                        .header("Authorization", "Bearer " + "invalid_token"))
                .andExpect(status().isForbidden());
    }
}

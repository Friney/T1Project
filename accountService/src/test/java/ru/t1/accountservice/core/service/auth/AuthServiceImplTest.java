package ru.t1.accountservice.core.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.t1.accountservice.api.dto.jwt.JwtAuthenticationDto;
import ru.t1.accountservice.api.dto.jwt.RefreshTokenDto;
import ru.t1.accountservice.api.dto.user.LoginRequest;
import ru.t1.accountservice.api.dto.user.UserChangePasswordRequest;
import ru.t1.accountservice.api.dto.user.UserDto;
import ru.t1.accountservice.api.dto.user.UserRegisterRequest;
import ru.t1.accountservice.api.dto.user.UserUpdateRequest;
import ru.t1.accountservice.core.entity.user.User;
import ru.t1.accountservice.core.exception.ServiceException;
import ru.t1.accountservice.core.service.jwt.JwtService;
import ru.t1.accountservice.core.service.jwt.version.JwtVersionService;
import ru.t1.accountservice.core.service.user.UserService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @Mock
    private JwtVersionService jwtVersionService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AuthServiceImpl authService;

    private final String testLogin = "testUser";
    private final String testPassword = "testPassword";
    private final Long testUserId = 1L;
    private User testUser;
    private UserDto testUserDto;
    private UserDetails testUserDetails;
    private JwtAuthenticationDto testJwtAuthDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(testUserId)
                .login(testLogin)
                .password(testPassword)
                .build();

        testUserDto = UserDto.builder()
                .id(testUserId)
                .login(testLogin)
                .build();

        testUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username(testLogin)
                .password(testPassword)
                .build();

        testJwtAuthDto = JwtAuthenticationDto.builder()
                .token("testToken")
                .refreshToken("testRefreshToken")
                .build();
    }

    @Test
    void loginSuccess() {
        LoginRequest loginRequest = new LoginRequest(testLogin, testPassword);

        when(userService.loadUserByUsername(testLogin)).thenReturn(testUserDetails);
        when(userService.getEntityByLogin(testLogin)).thenReturn(testUser);
        when(jwtVersionService.isExists(testUserId)).thenReturn(false);
        doNothing().when(jwtVersionService).createInitialVersion(testUserId);
        when(jwtService.generateAuthToken(testLogin)).thenReturn(testJwtAuthDto);

        JwtAuthenticationDto result = authService.login(loginRequest);

        assertEquals(testJwtAuthDto, result);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).loadUserByUsername(testLogin);
        verify(userService).getEntityByLogin(testLogin);
        verify(jwtVersionService).isExists(testUserId);
        verify(jwtVersionService).createInitialVersion(testUserId);
        verify(jwtService).generateAuthToken(testLogin);
    }

    @Test
    void loginWithExistingVersionSuccess() {
        LoginRequest loginRequest = new LoginRequest(testLogin, testPassword);

        when(userService.loadUserByUsername(testLogin)).thenReturn(testUserDetails);
        when(userService.getEntityByLogin(testLogin)).thenReturn(testUser);
        when(jwtVersionService.isExists(testUserId)).thenReturn(true);
        when(jwtService.generateAuthToken(testLogin)).thenReturn(testJwtAuthDto);

        JwtAuthenticationDto result = authService.login(loginRequest);

        assertEquals(testJwtAuthDto, result);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).loadUserByUsername(testLogin);
        verify(userService).getEntityByLogin(testLogin);
        verify(jwtVersionService).isExists(testUserId);
        verify(jwtVersionService, never()).createInitialVersion(any());
        verify(jwtService).generateAuthToken(testLogin);
    }

    @Test
    void registrationSuccess() {
        UserRegisterRequest registerRequest = new UserRegisterRequest(testLogin, testPassword, testPassword);

        when(passwordEncoder.encode(testPassword)).thenReturn("encodedPassword");
        when(userService.create(any(User.class))).thenReturn(testUserDto);
        doNothing().when(jwtVersionService).createInitialVersion(testUserId);

        UserDto result = authService.registration(registerRequest);

        assertEquals(testUserDto, result);
        verify(passwordEncoder).encode(testPassword);
        verify(userService).create(any(User.class));
        verify(jwtVersionService).createInitialVersion(testUserId);
    }

    @Test
    void registrationPasswordMismatch() {
        UserRegisterRequest registerRequest = new UserRegisterRequest(testLogin, testPassword, "differentPassword");

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authService.registration(registerRequest));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("passwords do not match"));
        verify(passwordEncoder, never()).encode(any());
        verify(userService, never()).create(any());
        verify(jwtVersionService, never()).createInitialVersion(any());
    }

    @Test
    void refreshSuccess() {
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto("validRefreshToken");

        when(jwtService.validateToken(refreshTokenDto.refreshToken())).thenReturn(true);
        when(jwtService.getLoginFromToken(refreshTokenDto.refreshToken())).thenReturn(testLogin);
        when(userService.getEntityByLogin(testLogin)).thenReturn(testUser);
        doNothing().when(jwtVersionService).incrementVersion(testUserId);
        when(jwtService.refreshBaseToken(testLogin, refreshTokenDto.refreshToken())).thenReturn(testJwtAuthDto);

        JwtAuthenticationDto result = authService.refresh(refreshTokenDto);

        assertEquals(testJwtAuthDto, result);
        verify(jwtService).validateToken(refreshTokenDto.refreshToken());
        verify(jwtService).getLoginFromToken(refreshTokenDto.refreshToken());
        verify(userService).getEntityByLogin(testLogin);
        verify(jwtVersionService).incrementVersion(testUserId);
        verify(jwtService).refreshBaseToken(testLogin, refreshTokenDto.refreshToken());
    }

    @Test
    void refreshInvalidToken() {
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto("invalidRefreshToken");

        when(jwtService.validateToken(refreshTokenDto.refreshToken())).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authService.refresh(refreshTokenDto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("invalid refresh token"));
        verify(jwtService).validateToken(refreshTokenDto.refreshToken());
        verify(jwtService, never()).getLoginFromToken(any());
        verify(userService, never()).getEntityByLogin(any());
        verify(jwtVersionService, never()).incrementVersion(any());
        verify(jwtService, never()).refreshBaseToken(any(), any());
    }

    @Test
    void refreshNullToken() {
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authService.refresh(refreshTokenDto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("invalid refresh token"));
        verify(jwtService, never()).validateToken(any());
        verify(jwtService, never()).getLoginFromToken(any());
        verify(userService, never()).getEntityByLogin(any());
        verify(jwtVersionService, never()).incrementVersion(any());
        verify(jwtService, never()).refreshBaseToken(any(), any());
    }

    @Test
    void changePasswordSuccess() {
        UserChangePasswordRequest changePasswordRequest = new UserChangePasswordRequest(
                testPassword,
                "newPassword",
                "newPassword"
        );

        when(userService.getEntityByLogin(testLogin)).thenReturn(testUser);
        when(passwordEncoder.matches(testPassword, testPassword)).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userService.update(any(User.class))).thenReturn(testUserDto);
        doNothing().when(jwtVersionService).incrementVersion(testUserId);

        UserDto result = authService.changePassword(changePasswordRequest, testUserDetails);

        assertEquals(testUserDto, result);
        verify(userService).getEntityByLogin(testLogin);
        verify(passwordEncoder).matches(testPassword, testPassword);
        verify(passwordEncoder).encode("newPassword");
        verify(userService).update(any(User.class));
        verify(jwtVersionService).incrementVersion(testUserId);
    }

    @Test
    void changePasswordMismatch() {
        UserChangePasswordRequest changePasswordRequest = new UserChangePasswordRequest(
                testPassword,
                "newPassword",
                "differentPassword"
        );

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authService.changePassword(changePasswordRequest, testUserDetails));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("passwords do not match"));
        verify(userService, never()).getEntityByLogin(any());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(passwordEncoder, never()).encode(any());
        verify(userService, never()).update(any());
        verify(jwtVersionService, never()).incrementVersion(any());
    }

    @Test
    void changePasswordIncorrectOldPassword() {
        UserChangePasswordRequest changePasswordRequest = new UserChangePasswordRequest(
                "wrongPassword",
                "newPassword",
                "newPassword"
        );

        when(userService.getEntityByLogin(testLogin)).thenReturn(testUser);
        when(passwordEncoder.matches("wrongPassword", testPassword)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authService.changePassword(changePasswordRequest, testUserDetails));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("old password is incorrect"));
        verify(userService).getEntityByLogin(testLogin);
        verify(passwordEncoder).matches("wrongPassword", testPassword);
        verify(passwordEncoder, never()).encode(any());
        verify(userService, never()).update(any());
        verify(jwtVersionService, never()).incrementVersion(any());
    }

    @Test
    void updateSuccess() {
        UserUpdateRequest updateRequest = new UserUpdateRequest("newLogin");

        User updatedUser = User.builder()
                .id(testUserId)
                .login("newLogin")
                .password(testPassword)
                .build();

        UserDto updatedUserDto = UserDto.builder()
                .id(testUserId)
                .login("newLogin")
                .build();

        when(userService.getEntityByLogin(testLogin)).thenReturn(testUser);
        when(userService.getEntityByLogin("newLogin")).thenReturn(updatedUser);
        when(userService.update(any(User.class))).thenReturn(updatedUserDto);
        doNothing().when(jwtVersionService).incrementVersion(testUserId);

        UserDto result = authService.update(updateRequest, testUserDetails);

        assertEquals(updatedUserDto, result);
        verify(userService).getEntityByLogin(testLogin);
        verify(userService).getEntityByLogin("newLogin");
        verify(userService).update(any(User.class));
        verify(jwtVersionService).incrementVersion(testUserId);
    }

    @Test
    void deleteSuccess() {
        when(userService.getEntityByLogin(testLogin)).thenReturn(testUser);
        doNothing().when(userService).delete(testUser);

        authService.delete(testUserDetails);

        verify(userService).getEntityByLogin(testLogin);
        verify(userService).delete(testUser);
    }
}

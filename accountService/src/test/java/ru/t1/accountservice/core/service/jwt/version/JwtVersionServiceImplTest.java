package ru.t1.accountservice.core.service.jwt.version;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import ru.t1.accountservice.core.entity.jwtversion.JwtVersion;
import ru.t1.accountservice.core.entity.user.User;
import ru.t1.accountservice.core.exception.ServiceException;
import ru.t1.accountservice.core.repository.JwtVersionRepository;
import ru.t1.accountservice.core.service.user.UserService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtVersionServiceImplTest {

    @Mock
    private JwtVersionRepository jwtVersionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private JwtVersionServiceImpl jwtVersionService;

    private User testUser;
    private JwtVersion testJwtVersion;
    private final String testLogin = "testUser";
    private final Long testUserId = 1L;
    private final Long testVersion = 1L;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(testUserId)
                .login(testLogin)
                .build();

        testJwtVersion = JwtVersion.builder()
                .version(testVersion)
                .user(testUser)
                .build();
    }

    @Test
    void createInitialVersionSuccess() {
        when(jwtVersionRepository.existsByUserId(testUserId)).thenReturn(false);
        when(jwtVersionRepository.save(any(JwtVersion.class))).thenReturn(testJwtVersion);

        jwtVersionService.createInitialVersion(testUserId);

        verify(jwtVersionRepository).existsByUserId(testUserId);
        verify(jwtVersionRepository).save(any(JwtVersion.class));
    }

    @Test
    void createInitialVersionAlreadyExists() {
        when(jwtVersionRepository.existsByUserId(testUserId)).thenReturn(true);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> jwtVersionService.createInitialVersion(testUserId));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("already exists"));
        verify(jwtVersionRepository).existsByUserId(testUserId);
        verify(jwtVersionRepository, never()).save(any(JwtVersion.class));
    }

    @Test
    void incrementVersionSuccess() {
        JwtVersion updatedVersion = JwtVersion.builder()
                .version(testVersion + 1)
                .user(testUser)
                .build();

        when(jwtVersionRepository.findByUserId(testUserId)).thenReturn(Optional.of(testJwtVersion));
        when(jwtVersionRepository.save(any(JwtVersion.class))).thenReturn(updatedVersion);

        jwtVersionService.incrementVersion(testUserId);

        verify(jwtVersionRepository).findByUserId(testUserId);
        verify(jwtVersionRepository).save(any(JwtVersion.class));
    }

    @Test
    void incrementVersionNotFound() {
        when(jwtVersionRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> jwtVersionService.incrementVersion(testUserId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(jwtVersionRepository).findByUserId(testUserId);
        verify(jwtVersionRepository, never()).save(any(JwtVersion.class));
    }

    @Test
    void getCurrentVersionSuccess() {
        when(userService.getEntityByLogin(testLogin)).thenReturn(testUser);
        when(jwtVersionRepository.findByUserId(testUserId)).thenReturn(Optional.of(testJwtVersion));

        Long result = jwtVersionService.getCurrentVersion(testLogin);

        assertEquals(testVersion, result);
        verify(userService).getEntityByLogin(testLogin);
        verify(jwtVersionRepository).findByUserId(testUserId);
    }

    @Test
    void getCurrentVersionUserNotFound() {
        when(userService.getEntityByLogin(testLogin))
                .thenThrow(new ServiceException("User not found", HttpStatus.NOT_FOUND));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> jwtVersionService.getCurrentVersion(testLogin));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(userService).getEntityByLogin(testLogin);
        verify(jwtVersionRepository, never()).findByUserId(any());
    }

    @Test
    void getCurrentVersionJwtVersionNotFound() {
        when(userService.getEntityByLogin(testLogin)).thenReturn(testUser);
        when(jwtVersionRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> jwtVersionService.getCurrentVersion(testLogin));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(userService).getEntityByLogin(testLogin);
        verify(jwtVersionRepository).findByUserId(testUserId);
    }

    @Test
    void isValidVersionSuccess() {
        when(userService.getEntityByLogin(testLogin)).thenReturn(testUser);
        when(jwtVersionRepository.findByUserId(testUserId)).thenReturn(Optional.of(testJwtVersion));

        boolean result = jwtVersionService.isValidVersion(testLogin, testVersion);

        assertTrue(result);
        verify(userService).getEntityByLogin(testLogin);
        verify(jwtVersionRepository).findByUserId(testUserId);
    }

    @Test
    void isValidVersionInvalidVersion() {
        when(userService.getEntityByLogin(testLogin)).thenReturn(testUser);
        when(jwtVersionRepository.findByUserId(testUserId)).thenReturn(Optional.of(testJwtVersion));

        boolean result = jwtVersionService.isValidVersion(testLogin, testVersion + 1);

        assertFalse(result);
        verify(userService).getEntityByLogin(testLogin);
        verify(jwtVersionRepository).findByUserId(testUserId);
    }

    @Test
    void isValidVersionUserNotFound() {
        when(userService.getEntityByLogin(testLogin))
                .thenThrow(new ServiceException("User not found", HttpStatus.NOT_FOUND));

        boolean result = jwtVersionService.isValidVersion(testLogin, testVersion);

        assertFalse(result);
        verify(userService).getEntityByLogin(testLogin);
        verify(jwtVersionRepository, never()).findByUserId(any());
    }

    @Test
    void isValidVersionJwtVersionNotFound() {
        when(userService.getEntityByLogin(testLogin)).thenReturn(testUser);
        when(jwtVersionRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

        boolean result = jwtVersionService.isValidVersion(testLogin, testVersion);

        assertFalse(result);
        verify(userService).getEntityByLogin(testLogin);
        verify(jwtVersionRepository).findByUserId(testUserId);
    }

    @Test
    void isExistsTrue() {
        when(jwtVersionRepository.existsByUserId(testUserId)).thenReturn(true);

        boolean result = jwtVersionService.isExists(testUserId);

        assertTrue(result);
        verify(jwtVersionRepository).existsByUserId(testUserId);
    }

    @Test
    void isExistsFalse() {
        when(jwtVersionRepository.existsByUserId(testUserId)).thenReturn(false);

        boolean result = jwtVersionService.isExists(testUserId);

        assertFalse(result);
        verify(jwtVersionRepository).existsByUserId(testUserId);
    }
}

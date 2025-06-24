package ru.t1.accountservice.core.service.user;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import ru.t1.accountservice.api.dto.user.UserDto;
import ru.t1.accountservice.core.entity.user.User;
import ru.t1.accountservice.core.exception.ServiceException;
import ru.t1.accountservice.core.mapper.UserMapper;
import ru.t1.accountservice.core.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDto testUserDto;
    private final String testLogin = "testUser";
    private final String testPassword = "testPassword";

    @BeforeEach
    void setUp() {
        Long testId = 1L;
        testUser = User.builder()
                .id(testId)
                .login(testLogin)
                .password(testPassword)
                .build();

        testUserDto = UserDto.builder()
                .id(testId)
                .login(testLogin)
                .build();
    }

    @Test
    void getEntityByLoginSuccess() {
        when(userRepository.findByLogin(testLogin)).thenReturn(Optional.of(testUser));

        User result = userService.getEntityByLogin(testLogin);

        assertEquals(testUser, result);
        verify(userRepository).findByLogin(testLogin);
    }

    @Test
    void getEntityByLoginNotFound() {
        when(userRepository.findByLogin(testLogin)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> userService.getEntityByLogin(testLogin));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(userRepository).findByLogin(testLogin);
    }

    @Test
    void createSuccess() {
        when(userRepository.existsByLogin(testLogin)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.map(testUser)).thenReturn(testUserDto);

        UserDto result = userService.create(testUser);

        assertEquals(testUserDto, result);
        verify(userRepository).existsByLogin(testLogin);
        verify(userRepository).save(testUser);
        verify(userMapper).map(testUser);
    }

    @Test
    void createUserAlreadyExists() {
        when(userRepository.existsByLogin(testLogin)).thenReturn(true);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> userService.create(testUser));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("already exists"));
        verify(userRepository).existsByLogin(testLogin);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateSuccess() {
        when(userRepository.findByLogin(testLogin)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.map(testUser)).thenReturn(testUserDto);

        UserDto result = userService.update(testUser);

        assertEquals(testUserDto, result);
        verify(userRepository).findByLogin(testLogin);
        verify(userRepository).save(testUser);
        verify(userMapper).map(testUser);
    }

    @Test
    void updateUserNotFound() {
        when(userRepository.findByLogin(testLogin)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> userService.update(testUser));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(userRepository).findByLogin(testLogin);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteSuccess() {
        when(userRepository.findByLogin(testLogin)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        userService.delete(testUser);

        verify(userRepository).findByLogin(testLogin);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUserNotFound() {
        when(userRepository.findByLogin(testLogin)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> userService.delete(testUser));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(userRepository).findByLogin(testLogin);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void loadUserByUsernameSuccess() {
        when(userRepository.findByLogin(testLogin)).thenReturn(Optional.of(testUser));

        UserDetails result = userService.loadUserByUsername(testLogin);

        assertEquals(testLogin, result.getUsername());
        assertEquals(testPassword, result.getPassword());
        assertTrue(result.getAuthorities().isEmpty());
        verify(userRepository).findByLogin(testLogin);
    }

    @Test
    void loadUserByUsernameNotFound() {
        when(userRepository.findByLogin(testLogin)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> userService.loadUserByUsername(testLogin));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("not found"));
        verify(userRepository).findByLogin(testLogin);
    }
}

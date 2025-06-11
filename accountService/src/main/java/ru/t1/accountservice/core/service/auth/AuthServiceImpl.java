package ru.t1.accountservice.core.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final JwtVersionService jwtVersionService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Override
    public JwtAuthenticationDto login(LoginRequest loginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.login(), loginRequest.password()));
        UserDetails user = userService.loadUserByUsername(loginRequest.login());
        // Мб тут проверить, что раз токена нет, то создать?
        Long userId = userService.getEntityByLogin(user.getUsername()).getId();
        if (!jwtVersionService.isExists(userId)) {
            jwtVersionService.createInitialVersion(userId);
        }
        return jwtService.generateAuthToken(user.getUsername());
    }

    @Override
    public UserDto registration(UserRegisterRequest userRegisterRequest) {
        if (!userRegisterRequest.password().equals(userRegisterRequest.confirmPassword())) {
            throw new ServiceException("passwords do not match", HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .login(userRegisterRequest.login())
                .password(passwordEncoder.encode(userRegisterRequest.password()))
                .build();

        UserDto userDto = userService.create(user);
        jwtVersionService.createInitialVersion(userDto.id());

        return userDto;
    }

    @Override
    public JwtAuthenticationDto refresh(RefreshTokenDto refreshTokenDto) {
        String refreshToken = refreshTokenDto.refreshToken();
        if (refreshToken == null || !jwtService.validateToken(refreshToken)) {
            throw new ServiceException("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        String login = jwtService.getLoginFromToken(refreshToken);
        Long userId = userService.getEntityByLogin(login).getId();
        jwtVersionService.incrementVersion(userId);
        return jwtService.refreshBaseToken(login, refreshToken);
    }

    @Override
    public UserDto changePassword(UserChangePasswordRequest userChangePasswordDto, UserDetails userDetails) {
        if (!userChangePasswordDto.newPassword().equals(userChangePasswordDto.confirmPassword())) {
            throw new ServiceException("passwords do not match", HttpStatus.BAD_REQUEST);
        }

        User user = userService.getEntityByLogin(userDetails.getUsername());
        if (!passwordEncoder.matches(userChangePasswordDto.oldPassword(), user.getPassword())) {
            throw new ServiceException("old password is incorrect", HttpStatus.BAD_REQUEST);
        }
        user.setPassword(passwordEncoder.encode(userChangePasswordDto.newPassword()));
        jwtVersionService.incrementVersion(user.getId());

        return userService.update(user);
    }

    @Override
    public UserDto update(UserUpdateRequest userUpdateDto, UserDetails userDetails) {
        User user = userService.getEntityByLogin(userDetails.getUsername());
        if (userUpdateDto.login() != null) {
            user.setLogin(userUpdateDto.login());
        }
        Long userId = userService.getEntityByLogin(userUpdateDto.login()).getId();
        jwtVersionService.incrementVersion(userId);

        return userService.update(user);
    }

    @Override
    public void delete(UserDetails userDetails) {
        User user = userService.getEntityByLogin(userDetails.getUsername());
        userService.delete(user);
    }
}

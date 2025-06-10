package ru.t1.accountservice.core.service.auth;

import org.springframework.security.core.userdetails.UserDetails;
import ru.t1.accountservice.api.dto.jwt.JwtAuthenticationDto;
import ru.t1.accountservice.api.dto.jwt.RefreshTokenDto;
import ru.t1.accountservice.api.dto.user.LoginRequest;
import ru.t1.accountservice.api.dto.user.UserChangePasswordRequest;
import ru.t1.accountservice.api.dto.user.UserDto;
import ru.t1.accountservice.api.dto.user.UserRegisterRequest;
import ru.t1.accountservice.api.dto.user.UserUpdateRequest;

public interface AuthService {

    JwtAuthenticationDto login(LoginRequest loginRequest);

    UserDto registration(UserRegisterRequest userRegisterRequest);

    JwtAuthenticationDto refresh(RefreshTokenDto refreshTokenDto);

    UserDto changePassword(UserChangePasswordRequest userChangePasswordDto, UserDetails userDetails);

    UserDto update(UserUpdateRequest userUpdateDto, UserDetails userDetails);

    void delete(UserDetails userDetails);
}

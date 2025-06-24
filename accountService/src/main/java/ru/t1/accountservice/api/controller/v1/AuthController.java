package ru.t1.accountservice.api.controller.v1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.accountservice.api.Paths;
import ru.t1.accountservice.api.dto.jwt.JwtAuthenticationDto;
import ru.t1.accountservice.api.dto.jwt.RefreshTokenDto;
import ru.t1.accountservice.api.dto.user.LoginRequest;
import ru.t1.accountservice.api.dto.user.UserChangePasswordRequest;
import ru.t1.accountservice.api.dto.user.UserDto;
import ru.t1.accountservice.api.dto.user.UserRegisterRequest;
import ru.t1.accountservice.api.dto.user.UserUpdateRequest;
import ru.t1.accountservice.core.service.auth.AuthService;

@RestController
@RequestMapping(Paths.AUTH_V1)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public JwtAuthenticationDto login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/refresh")
    public JwtAuthenticationDto refresh(@Valid @RequestBody RefreshTokenDto refreshTokenDto) {
        return authService.refresh(refreshTokenDto);
    }

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        return authService.registration(userRegisterRequest);
    }

    @PatchMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public UserDto changePassword(@Valid @RequestBody UserChangePasswordRequest userChangePasswordDto, @AuthenticationPrincipal UserDetails userDetails) {
        return authService.changePassword(userChangePasswordDto, userDetails);
    }

    @PatchMapping
    public UserDto update(@Valid @RequestBody UserUpdateRequest userUpdateDto, @AuthenticationPrincipal UserDetails userDetails) {
        return authService.update(userUpdateDto, userDetails);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserDetails userDetails) {
        authService.delete(userDetails);
    }
}

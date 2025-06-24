package ru.t1.accountservice.api.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserRegisterRequest(
        @NotBlank String login,
        @NotBlank String password,
        @NotBlank String confirmPassword
) {
}

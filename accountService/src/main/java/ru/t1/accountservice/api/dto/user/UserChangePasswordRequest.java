package ru.t1.accountservice.api.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserChangePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank String newPassword,
        @NotBlank String confirmPassword
) {
}

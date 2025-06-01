package ru.t1.accountservice.api.dto.client;

import jakarta.validation.constraints.NotBlank;

public record ClientCreateRequest(
        @NotBlank String firstName,
        String middleName,
        @NotBlank String lastName
) {
}

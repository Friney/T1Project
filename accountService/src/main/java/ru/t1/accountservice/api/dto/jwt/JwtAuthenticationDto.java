package ru.t1.accountservice.api.dto.jwt;

import lombok.Builder;

@Builder
public record JwtAuthenticationDto(
        String token,
        String refreshToken
) {
}

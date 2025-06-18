package ru.t1.accountservice.api.dto.user;

import lombok.Builder;

@Builder
public record UserDto(
        Long id,
        String login
) {
}

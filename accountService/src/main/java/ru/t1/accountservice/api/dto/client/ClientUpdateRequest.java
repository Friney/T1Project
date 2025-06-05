package ru.t1.accountservice.api.dto.client;

public record ClientUpdateRequest(
        String firstName,
        String middleName,
        String lastName
) {
}

package ru.t1.accountservice.api.dto.datasource;

import lombok.Builder;

@Builder
public record DataSourceErrorLogDto(
        String stackTrace,
        String errorMessage,
        String methodSignature
) {
}

package ru.t1.accountservice.core.service.jwt.version;

import ru.t1.accountservice.core.entity.jwtversion.JwtVersion;

public interface JwtVersionService {
    JwtVersion getVersionByUserId(Long userId);

    void createInitialVersion(Long userId);

    void incrementVersion(Long userId);

    Long getCurrentVersion(String login);

    boolean isValidVersion(String login, Long tokenVersion);

    boolean isExists(Long userId);
}
package ru.t1.accountservice.core.service.jwt.version;

public interface JwtVersionService {

    void createInitialVersion(Long userId);

    void incrementVersion(Long userId);

    Long getCurrentVersion(String login);

    boolean isValidVersion(String login, Long tokenVersion);

    boolean isExists(Long userId);
}
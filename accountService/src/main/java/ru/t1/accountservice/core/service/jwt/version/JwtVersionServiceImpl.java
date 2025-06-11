package ru.t1.accountservice.core.service.jwt.version;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.accountservice.core.annotation.LogDataSourceError;
import ru.t1.accountservice.core.annotation.Metric;
import ru.t1.accountservice.core.entity.jwtversion.JwtVersion;
import ru.t1.accountservice.core.entity.user.User;
import ru.t1.accountservice.core.exception.ServiceException;
import ru.t1.accountservice.core.repository.JwtVersionRepository;
import ru.t1.accountservice.core.service.user.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtVersionServiceImpl implements JwtVersionService {
    private final JwtVersionRepository jwtVersionRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public JwtVersion getVersionByUserId(Long userId) {
        return jwtVersionRepository.findByUserId(userId)
                .orElseThrow(() -> new ServiceException("Token version not found for user id: " + userId, HttpStatus.NOT_FOUND));
    }

    @Override
    @Metric
    @LogDataSourceError
    @Transactional
    public void createInitialVersion(Long userId) {
        if (jwtVersionRepository.existsByUserId(userId)) {
            throw new ServiceException(
                    "Token version already exists for user: " + userId,
                    HttpStatus.BAD_REQUEST
            );
        }

        User user = User.builder()
                .id(userId)
                .build();
        JwtVersion version = JwtVersion.builder()
                .version(1L)
                .user(user)
                .build();

        jwtVersionRepository.save(version);
    }

    @Override
    @Metric
    @LogDataSourceError
    @Transactional
    public void incrementVersion(Long userId) {
        JwtVersion version = getVersionByUserId(userId);

        version.setVersion(version.getVersion() + 1);

        jwtVersionRepository.save(version);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCurrentVersion(String login) {
        User user = userService.getEntityByLogin(login);
        return getVersionByUserId(user.getId()).getVersion();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidVersion(String login, Long tokenVersion) {
        try {
            Long currentVersion = getCurrentVersion(login);
            return currentVersion.equals(tokenVersion);
        } catch (ServiceException e) {
            log.warn("Failed to validate token version for user {}: {}", login, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isExists(Long userId) {
        return jwtVersionRepository.existsByUserId(userId);
    }
}
package ru.t1.accountservice.core.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.accountservice.api.dto.user.UserDto;
import ru.t1.accountservice.core.entity.user.User;
import ru.t1.accountservice.core.exception.ServiceException;
import ru.t1.accountservice.core.mapper.UserMapper;
import ru.t1.accountservice.core.repository.UserRepository;
import ru.t1.monitoringstarter.core.annotation.LogDataSourceError;
import ru.t1.monitoringstarter.core.annotation.Metric;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public User getEntityByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new ServiceException("User with login '" + login + "' not found", HttpStatus.NOT_FOUND));
    }

    @Override
    @Metric
    @LogDataSourceError
    @Transactional
    public UserDto create(User userForCreate) {
        if (userRepository.existsByLogin(userForCreate.getLogin())) {
            throw new ServiceException("User with login '" + userForCreate.getLogin() + "' already exists", HttpStatus.BAD_REQUEST);
        }

        return userMapper.map(userRepository.save(userForCreate));
    }

    @Override
    @Metric
    @LogDataSourceError
    @Transactional
    public UserDto update(User userForUpdate) {
        User user = getEntityByLogin(userForUpdate.getLogin());
        return userMapper.map(userRepository.save(user));
    }

    @Override
    @Metric
    @LogDataSourceError
    @Transactional
    public void delete(User userForDelete) {
        User user = getEntityByLogin(userForDelete.getLogin());
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        User user = getEntityByLogin(username);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getLogin())
                .password(user.getPassword())
                .build();
    }
}

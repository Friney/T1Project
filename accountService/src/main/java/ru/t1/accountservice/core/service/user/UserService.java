package ru.t1.accountservice.core.service.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import ru.t1.accountservice.api.dto.user.UserDto;
import ru.t1.accountservice.core.entity.user.User;

public interface UserService extends UserDetailsService {

    UserDto create(User userForCreate);

    UserDto update(User userForUpdate);

    void delete(User userForDelete);

    User getEntityByLogin(String login);

    @Override
    UserDetails loadUserByUsername(String username);
}

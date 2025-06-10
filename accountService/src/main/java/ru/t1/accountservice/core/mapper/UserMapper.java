package ru.t1.accountservice.core.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.t1.accountservice.api.dto.user.UserDto;
import ru.t1.accountservice.core.entity.user.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserDto map(User client);

    List<UserDto> map(List<User> clients);
}

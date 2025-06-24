package ru.t1.accountservice.core.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.t1.accountservice.api.dto.client.ClientDto;
import ru.t1.accountservice.core.entity.client.Client;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = AccountMapper.class)
public interface ClientMapper {

    @Mapping(target = "id", source = "clientId")
    ClientDto map(Client client);

    @Mapping(target = "id", source = "clientId")
    List<ClientDto> map(List<Client> clients);
}

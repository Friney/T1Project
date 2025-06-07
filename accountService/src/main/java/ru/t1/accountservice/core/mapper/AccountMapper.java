package ru.t1.accountservice.core.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.t1.accountservice.api.dto.account.AccountDto;
import ru.t1.accountservice.core.entity.account.Account;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = TransactionMapper.class)
public interface AccountMapper {

    @Mapping(target = "id", source = "accountId")
    AccountDto map(Account account);

    @Mapping(target = "id", source = "accountId")
    List<AccountDto> map(List<Account> accounts);
}

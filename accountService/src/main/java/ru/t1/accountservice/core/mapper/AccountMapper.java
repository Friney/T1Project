package ru.t1.accountservice.core.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.t1.accountservice.api.dto.account.AccountDto;
import ru.t1.accountservice.core.entity.account.Account;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = TransactionMapper.class)
public interface AccountMapper {

    AccountDto map(Account account);

    List<AccountDto> map(List<Account> accounts);
}

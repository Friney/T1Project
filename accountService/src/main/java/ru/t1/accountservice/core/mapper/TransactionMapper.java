package ru.t1.accountservice.core.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.t1.accountservice.api.dto.transaction.TransactionDto;
import ru.t1.accountservice.core.entity.transaction.Transaction;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {

    @Mapping(target = "id", source = "transactionId")
    TransactionDto map(Transaction transaction);

    @Mapping(target = "id", source = "transactionId")
    List<TransactionDto> map(List<Transaction> transactions);
}

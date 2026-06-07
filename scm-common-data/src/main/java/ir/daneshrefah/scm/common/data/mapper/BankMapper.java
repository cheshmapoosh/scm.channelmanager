package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.bank.BankEntity;
import ir.daneshrefah.scm.common.data.dto.bank.BankDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {})
public interface BankMapper {
    @Mapping(target = "name", ignore = true)
    BankEntity toEntity(BankDto bankDTO);

    BankDto toDto(BankEntity bankEntity);

    default BankEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        BankEntity bank = new BankEntity();
        bank.setId(id);
        return bank;
    }
}

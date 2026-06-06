package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.cardPrefix.CardPrefixEntity;
import ir.daneshrefah.scm.common.data.dto.card.CardPrefixDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {BankMapper.class})
public interface CardPrefixMapper {
    @Mapping(source = "bank.id", target = "bankId")
    CardPrefixDto toDto(CardPrefixEntity cardPrefix);

    @Mapping(source = "bankId", target = "bank")
    CardPrefixEntity toEntity(CardPrefixDto cardPrefixDTO);

    default CardPrefixEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        CardPrefixEntity cardPrefix = new CardPrefixEntity();
        cardPrefix.setId(id);
        return cardPrefix;
    }
}

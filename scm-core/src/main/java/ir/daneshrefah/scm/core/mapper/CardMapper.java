package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.customer.Card;
import ir.daneshrefah.scm.core.entity.person.CardEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING)
public interface CardMapper {

    @Mapping(source = "cardNo", target = "cardNumber")
    @Mapping(target = "cardType", ignore = true)
    Card toModel(CardEntity entity);

    List<Card> toModels(List<CardEntity> entities);
}

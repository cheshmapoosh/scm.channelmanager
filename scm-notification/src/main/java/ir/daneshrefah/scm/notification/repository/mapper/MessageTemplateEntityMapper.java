package ir.daneshrefah.scm.notification.repository.mapper;

import ir.daneshrefah.scm.notification.domain.MessageTemplateEntity;
import ir.daneshrefah.scm.common.model.notification.MessageTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MessageTemplateEntityMapper {

    MessageTemplateEntityMapper INSTANCE = Mappers.getMapper(MessageTemplateEntityMapper.class);
    MessageTemplateEntity toEntity(MessageTemplate messageTemplate);
    MessageTemplate toDto(MessageTemplateEntity messageTemplateEntity);


}
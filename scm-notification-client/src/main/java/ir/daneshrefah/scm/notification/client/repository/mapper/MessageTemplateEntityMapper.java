package ir.daneshrefah.scm.notification.client.repository.mapper;

import ir.daneshrefah.scm.common.model.notification.MessageTemplate;
import ir.daneshrefah.scm.notification.client.repository.domain.MessageTemplateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MessageTemplateEntityMapper {

    MessageTemplateEntityMapper INSTANCE = Mappers.getMapper(MessageTemplateEntityMapper.class);
    MessageTemplateEntity toEntity(MessageTemplate messageTemplate);
    MessageTemplate toDto(MessageTemplateEntity messageTemplateEntity);


}
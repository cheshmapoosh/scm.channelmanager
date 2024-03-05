package ir.daneshrefah.scm.common.data.mapper.notification;

import ir.daneshrefah.scm.common.data.entity.notification.MessageTemplateEntity;
import ir.daneshrefah.scm.common.model.notification.MessageTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MessageTemplateMapper {

    MessageTemplateMapper INSTANCE = Mappers.getMapper(MessageTemplateMapper.class);
    MessageTemplateEntity toEntity(MessageTemplate messageTemplate);
    MessageTemplate toDto(MessageTemplateEntity messageTemplateEntity);


}
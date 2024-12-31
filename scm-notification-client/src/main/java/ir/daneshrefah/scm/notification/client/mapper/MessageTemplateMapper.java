package ir.daneshrefah.scm.notification.client.mapper;

import ir.daneshrefah.scm.common.model.notification.MessageTemplate;
import ir.daneshrefah.scm.notification.client.repository.entity.MessageTemplateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MessageTemplateMapper {

    MessageTemplateMapper INSTANCE = Mappers.getMapper(MessageTemplateMapper.class);
    MessageTemplateEntity toEntity(MessageTemplate messageTemplate);
    MessageTemplate toModel(MessageTemplateEntity messageTemplateEntity);


}
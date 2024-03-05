package ir.daneshrefah.scm.common.data.mapper.notification;

import ir.daneshrefah.scm.common.data.entity.notification.NotificationEntity;
import ir.daneshrefah.scm.common.model.notification.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface NotificationMapper {

    NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

    @Mapping(source = "messageTemplate",target = "messageTemplateEntity")
    NotificationEntity toEntity(Notification notification);

    @Mapping(source = "messageTemplateEntity",target = "messageTemplate")
    Notification toModel(NotificationEntity notificationEntity);

    List<Notification> toModel(List<NotificationEntity> notificationEntities);


}
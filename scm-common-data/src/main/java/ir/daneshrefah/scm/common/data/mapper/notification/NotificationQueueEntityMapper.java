package ir.daneshrefah.scm.common.data.mapper.notification;

import ir.daneshrefah.scm.common.data.entity.notification.NotificationQueueEntity;
import ir.daneshrefah.scm.common.model.notification.NotificationQueueModel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface NotificationQueueEntityMapper {

    NotificationQueueEntityMapper INSTANCE = Mappers.getMapper(NotificationQueueEntityMapper.class);

    NotificationQueueEntity toEntity(NotificationQueueModel notificationQueueModel);

    NotificationQueueModel toModel(NotificationQueueEntity notificationQueueEntity);

    List<NotificationQueueModel> toModel(List<NotificationQueueEntity> notificationQueueEntity);


}
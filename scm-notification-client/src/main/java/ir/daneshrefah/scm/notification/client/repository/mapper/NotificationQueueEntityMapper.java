package ir.daneshrefah.scm.notification.client.repository.mapper;

import ir.daneshrefah.scm.common.model.notification.NotificationQueueModel;
import ir.daneshrefah.scm.notification.client.repository.domain.NotificationQueueEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface NotificationQueueEntityMapper {

    NotificationQueueEntityMapper INSTANCE = Mappers.getMapper(NotificationQueueEntityMapper.class);

    NotificationQueueEntity toEntity(NotificationQueueModel notificationQueueModel);

    NotificationQueueModel toModel(NotificationQueueEntity notificationQueueEntity);

}
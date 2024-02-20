package ir.daneshrefah.scm.notification.client.repository.converters;


import ir.daneshrefah.scm.common.model.notification.NotificationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class NotificationStatusConverter implements AttributeConverter<NotificationStatus,Integer> {
    @Override
    public Integer convertToDatabaseColumn(NotificationStatus notificationStatus) {
        return notificationStatus.getCode();
    }

    @Override
    public NotificationStatus convertToEntityAttribute(Integer integer) {
        return NotificationStatus.findByCode(integer);
    }
}

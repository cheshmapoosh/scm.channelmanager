package ir.daneshrefah.scm.notification.repository.converters;


import ir.daneshrefah.scm.common.model.notification.NotificationData;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class NotificationDataConverter implements AttributeConverter<NotificationData,String> {


    @Override
    public String convertToDatabaseColumn(NotificationData notificationData) {
        return notificationData.toString();
    }

    @Override
    public NotificationData convertToEntityAttribute(String s) {
        NotificationData notificationData = new NotificationData();
        notificationData.fillValues(s);
        return notificationData;
    }
}

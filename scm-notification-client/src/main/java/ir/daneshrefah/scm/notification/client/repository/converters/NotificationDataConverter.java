package ir.daneshrefah.scm.notification.client.repository.converters;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ir.daneshrefah.scm.common.model.notification.NotificationData;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;

@Converter
public class NotificationDataConverter implements AttributeConverter<NotificationData, String> {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    @Override
    @SneakyThrows
    public String convertToDatabaseColumn(NotificationData notificationData) {
        return OBJECT_MAPPER.writeValueAsString(notificationData);
    }

    @Override
    @SneakyThrows
    public NotificationData convertToEntityAttribute(String s) {
        return OBJECT_MAPPER.readValue(s, NotificationData.class);
    }

}

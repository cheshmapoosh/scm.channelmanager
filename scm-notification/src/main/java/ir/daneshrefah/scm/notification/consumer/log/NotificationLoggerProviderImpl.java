package ir.daneshrefah.scm.notification.consumer.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ir.daneshrefah.scm.common.model.notification.NotificationLog;
import ir.daneshrefah.scm.utils.log.LogUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class NotificationLoggerProviderImpl implements NotificationLogProvider {

    private final static ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    @Override
    @SneakyThrows
    public void log(NotificationLog notificationLog) {
        //TODO >>>  sending log to centralized log service ...
         log.info(LogUtils.markWith(LogUtils.Color.BLUE,"[SNT-CORE-NOTIFICATION-LOG] "+OBJECT_MAPPER.writeValueAsString(notificationLog)));
    }
}

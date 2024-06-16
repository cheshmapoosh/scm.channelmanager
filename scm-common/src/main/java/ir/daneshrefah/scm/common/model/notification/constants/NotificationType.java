package ir.daneshrefah.scm.common.model.notification.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NotificationType {
    SMS_MQ_DEFAULT("sendGeneralSMS");
    private final String type;
}

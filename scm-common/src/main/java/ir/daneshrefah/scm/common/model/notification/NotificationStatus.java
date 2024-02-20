package ir.daneshrefah.scm.common.model.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum NotificationStatus {
    DRAFT(1),
    QUEUE(2),
    SENDING(3),
    RECEIVED(4),
    RE_TRYING(5),
    FAILED(6);
    private final int code;

    public static NotificationStatus findByCode(int code) {
        return Arrays.stream(values())
                .filter(notificationStatus -> notificationStatus.getCode() == code)
                .findFirst()
                .orElse(null);
    }
}

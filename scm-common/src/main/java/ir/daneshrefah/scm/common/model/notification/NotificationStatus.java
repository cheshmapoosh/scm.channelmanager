package ir.daneshrefah.scm.common.model.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum NotificationStatus {
    DRAFT(1),
    SENDING(2),
    RECEIVED(3),
    RE_TRYING(4),
    FAILED(5);
    private final int code;

    public static NotificationStatus findByCode(int code) {
        return Arrays.stream(values())
                .filter(notificationStatus -> notificationStatus.getCode() == code)
                .findFirst()
                .orElse(null);
    }
}

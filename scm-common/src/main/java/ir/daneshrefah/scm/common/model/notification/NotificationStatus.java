package ir.daneshrefah.scm.common.model.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationStatus {
    DRAFT,
    QUEUE,
    SENT,
    RE_TRYING,
    FAILED

}

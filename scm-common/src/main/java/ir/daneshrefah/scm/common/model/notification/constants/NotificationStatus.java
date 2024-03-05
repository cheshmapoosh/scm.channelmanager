package ir.daneshrefah.scm.common.model.notification.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationStatus {
    DRAFT,
    QUEUE,
    SENDING,
    SENT,
    RE_TRYING,
    FAILED

}

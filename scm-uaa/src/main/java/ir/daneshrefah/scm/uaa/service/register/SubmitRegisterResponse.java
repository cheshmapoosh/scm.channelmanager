package ir.daneshrefah.scm.uaa.service.register;

import ir.daneshrefah.scm.common.model.notification.NotificationMedia;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-18
 */
@Builder
@Getter
public class SubmitRegisterResponse implements Serializable {

    private final String clientId;
    private final String recipient;
    private final NotificationMedia media;
    private final Instant expireTime;
    private final boolean isSuccessful;
}

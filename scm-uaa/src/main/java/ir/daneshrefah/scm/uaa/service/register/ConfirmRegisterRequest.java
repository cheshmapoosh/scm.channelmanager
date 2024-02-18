package ir.daneshrefah.scm.uaa.service.register;

import ir.daneshrefah.scm.common.model.notification.NotificationMedia;
import lombok.Data;

import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-18
 */
@Data
public class ConfirmRegisterRequest implements Serializable {

    private String clientId;
    private String clientVersion;
    private String clientSignature;
    private String recipient;
    private NotificationMedia media;
    private String claimCode;

}

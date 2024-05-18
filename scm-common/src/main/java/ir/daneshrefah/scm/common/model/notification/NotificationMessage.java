package ir.daneshrefah.scm.common.model.notification;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
@Getter
@Setter
@Accessors(chain = true)
public class NotificationMessage implements Serializable {

    private NotificationRequest request;
    private String payload;

}

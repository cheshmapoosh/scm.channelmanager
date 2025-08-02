package ir.daneshrefah.scm.common.model.notification;

import ir.daneshrefah.scm.common.AbstractModel;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationTemplate;
import ir.daneshrefah.scm.common.model.notification.constants.TemplateFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Setter
@Getter
public class MessageTemplate extends AbstractModel<Long> {
    //TODO CREATOR AND EDITOR TYPE MUST CHANGED TO INTEGER ON DB

    private NotificationTemplate code;
    private String title;
    private String body;
    private Integer maxTryCount;
    private Integer maxMinutesExpiration;
    private TemplateFormat templateFormat;
    private boolean isSystemic;
    private Locale locale;


}

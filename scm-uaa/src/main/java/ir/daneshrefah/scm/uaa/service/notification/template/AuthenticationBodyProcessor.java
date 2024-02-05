package ir.daneshrefah.scm.uaa.service.notification.template;

import ir.daneshrefah.scm.uaa.domain.notification.MessageTemplate;
import ir.daneshrefah.scm.uaa.domain.notification.NotificationData;
import ir.daneshrefah.scm.uaa.service.notification.NotificationConstants;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
@Component
public class AuthenticationBodyProcessor extends NotificationBodyProcessor {

    @Override
    protected String processInternal(MessageTemplate template, NotificationData data) {
        return null;
    }

    @Override
    protected boolean support(MessageTemplate template) {
        return NotificationConstants.MESSAGE_TEMPLATE_CODE_AUTHENTICATION.equals(template.getCode());
    }

}

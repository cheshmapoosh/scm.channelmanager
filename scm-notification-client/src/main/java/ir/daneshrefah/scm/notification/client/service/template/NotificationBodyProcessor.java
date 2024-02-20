package ir.daneshrefah.scm.notification.client.service.template;


import ir.daneshrefah.scm.common.model.notification.MessageTemplate;
import ir.daneshrefah.scm.common.model.notification.NotificationData;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
public abstract class NotificationBodyProcessor {

    public String process(MessageTemplate template, NotificationData data, NotificationRequest request) {
        if (!support(template))
            return null;
        return processInternal(template, data,request);
    }

    protected abstract String processInternal(MessageTemplate template, NotificationData data,NotificationRequest request);

    protected abstract boolean support(MessageTemplate template);

}

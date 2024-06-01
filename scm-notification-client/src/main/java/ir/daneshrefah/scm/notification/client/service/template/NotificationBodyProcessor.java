package ir.daneshrefah.scm.notification.client.service.template;


import ir.daneshrefah.scm.common.model.notification.MessageTemplate;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.common.model.notification.constants.TemplateFormat;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
@RequiredArgsConstructor
public abstract class NotificationBodyProcessor {

    private final NotificationDictionary dictionary;
    public String process(MessageTemplate template, NotificationRequest request) {
        if (!support(template.getTemplateFormat()))
            return null;
        return processInternal(template, request);
    }

    protected final String extractRequestValue(NotificationRequest request, String key) {
        return dictionary.extractRequestValue(request, key);
    }

    protected abstract String processInternal(MessageTemplate template, NotificationRequest request);

    protected abstract boolean support(TemplateFormat template);

}

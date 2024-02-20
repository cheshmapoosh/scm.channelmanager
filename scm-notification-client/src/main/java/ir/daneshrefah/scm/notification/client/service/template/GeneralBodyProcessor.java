package ir.daneshrefah.scm.notification.client.service.template;


import ir.daneshrefah.scm.common.model.notification.DataKey;
import ir.daneshrefah.scm.common.model.notification.MessageTemplate;
import ir.daneshrefah.scm.common.model.notification.NotificationData;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.common.model.notification.constants.TemplateCode;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
@Component
public class GeneralBodyProcessor extends NotificationBodyProcessor {

    private static final Map<MessageTemplate, Set<String>> TEMPLATE_PARAMETERS_CACHE = new ConcurrentHashMap<>();
    private static final String START_TAG = "<%";
    private static final String END_TAG = "%>";
    private static final String LINE_SEPARATOR = "lineSeparator";
    private static final String TERMINAL_CODE = "terminalCode";
    private static final String TERMINAL_TITLE = "terminalTitle";

    @Override
    protected String processInternal(MessageTemplate template, NotificationData data, NotificationRequest request) {
        String templateBody = normalizeTemplateText(template.getBody());
        if (isFixedMessage(templateBody)) {
            return templateBody;
        }
        Set<String> templateParameters = getCacheableTemplateParameters(template);
        for (String templateParameter : templateParameters) {
            templateBody = templateBody.replace(START_TAG + templateParameter +END_TAG, getParameterValue(data, templateParameter, request));
        }
        return templateBody;
    }

    private String normalizeTemplateText(String body) {
        return body.replace("{", START_TAG)
                .replace("}", END_TAG)
                .replace("$", "");
    }

    private Set<String> getCacheableTemplateParameters(MessageTemplate messageTemplate) {
        return TEMPLATE_PARAMETERS_CACHE
                .computeIfAbsent(messageTemplate, template -> StringUtils.findTemplateParameters(template.getBody()));
    }

    private String getParameterValue(NotificationData data, String templateParameter, NotificationRequest request) {
        return switch (templateParameter) {
            case LINE_SEPARATOR -> System.lineSeparator();
            case TERMINAL_CODE -> request.getTerminalCode();
            case TERMINAL_TITLE -> request.getTerminalTittle();
            default -> String.valueOf(data.get(Objects.requireNonNull(DataKey.findByParameterName(templateParameter))));
        };
    }

    private boolean isFixedMessage(String templateBody) {
        return !(templateBody.contains(START_TAG));
    }

    @Override
    protected boolean support(MessageTemplate template) {
        //TODO changed after completed
        return TemplateCode.AUTHENTICATION.equals(template.getCode());
    }

}

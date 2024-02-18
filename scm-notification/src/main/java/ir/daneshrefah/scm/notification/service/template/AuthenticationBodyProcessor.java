package ir.daneshrefah.scm.notification.service.template;


import ir.daneshrefah.scm.common.model.notification.DataKey;
import ir.daneshrefah.scm.common.model.notification.MessageTemplate;
import ir.daneshrefah.scm.common.model.notification.NotificationData;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationConstants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
@Component
public class AuthenticationBodyProcessor extends NotificationBodyProcessor {

    private static final String START_TAG_SIGN = "${";
    private static final String END_TAG_SIGN = "}";

    @Override
    protected String processInternal(MessageTemplate template, NotificationData data) {
        String templateBody = template.getBody();
        Set<String> templateParameters = StringUtils.findTemplateParameters(templateBody);
        for (String templateParameter : templateParameters) {
            templateBody = templateBody.replace(START_TAG_SIGN+templateParameter+END_TAG_SIGN,String.valueOf(data.get(Objects.requireNonNull(DataKey.findByParameterName(templateParameter)))));
        }
        return templateBody;
    }

    @Override
    protected boolean support(MessageTemplate template) {
        return NotificationConstants.MESSAGE_TEMPLATE_CODE_AUTHENTICATION.equals(template.getCode());
    }

}

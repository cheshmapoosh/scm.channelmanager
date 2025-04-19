package ir.daneshrefah.scm.notification.client.service.template;


import ir.daneshrefah.scm.common.model.notification.MessageTemplate;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.common.model.notification.constants.TemplateFormat;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-01
 */
@Component
public class VelocityBodyProcessor extends NotificationBodyProcessor {

    private static final Pattern PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    private static final Map<Long, Template> TEMPLATE_MAP = new HashMap<>();

    public VelocityBodyProcessor(NotificationDictionary dictionary) {
        super(dictionary);
    }

    @Override
    protected String processInternal(MessageTemplate template, NotificationRequest request) {
        Template velocityTemplate = findTemplate(template);
        if (Objects.isNull(velocityTemplate)) {
            return null;
        }
        List<String> parameters = extractParameterNames(template);
        Map<String, Object> data = new HashMap<>();
        parameters.forEach(parameter -> data.put(parameter, extractRequestValue(request, parameter)));
        StringWriter writer = new StringWriter();
        VelocityContext context = new VelocityContext(data);
        velocityTemplate.merge(context, writer);
        return writer.toString();
    }

    private Template findTemplate(MessageTemplate template) {
        if (!TEMPLATE_MAP.containsKey(template.getId())) {
            RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
            StringReader reader = new StringReader(template.getBody());
            Template velocityTemplate = new Template();
            velocityTemplate.setRuntimeServices(runtimeServices);
            try {
                velocityTemplate.setData(runtimeServices.parse(reader, String.valueOf(template.getId())));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            velocityTemplate.initDocument();
            TEMPLATE_MAP.put(template.getId(), velocityTemplate);
        }
        return TEMPLATE_MAP.get(template.getId());
    }

    private List<String> extractParameterNames(MessageTemplate template) {
        Matcher matcher = PATTERN.matcher(template.getBody());
        List<String> parameters = new ArrayList<>();
        while (matcher.find()) {
            parameters.add(matcher.group(1));
        }
        return parameters;
    }

    @Override
    protected boolean support(TemplateFormat template) {
        return TemplateFormat.VELOCITY.equals(template);
    }

}

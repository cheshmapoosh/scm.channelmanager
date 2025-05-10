package ir.daneshrefah.scm.core.integration.template;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;

import java.util.Map;

public class ConditionalServiceMetadataTemplate extends AbstractServiceMetadataTemplate {
    private final ServiceMetadataTemplate delegateTemplate;
    private final ServiceMetadataTemplate conditionTemplate;

    public ConditionalServiceMetadataTemplate(ServiceMetadataTemplate delegateTemplate,
                                              ServiceMetadataTemplate conditionTemplate) {
        this.delegateTemplate = delegateTemplate;
        this.conditionTemplate = conditionTemplate;
    }

    public boolean matches(Message message) {
        String result = conditionTemplate.render(message);
        return Boolean.parseBoolean(result.trim());
    }

    @Override
    public String render(Message message) {
        if (!matches(message)) {
            throw new IllegalStateException("Conditional template does not match");
        }
        return delegateTemplate.render(message);
    }

    @Override
    public JsonNode renderAsJson(Message message) {
        if (!matches(message)) {
            throw new IllegalStateException("Conditional template does not match");
        }
        return delegateTemplate.renderAsJson(message);
    }

    @Override
    public Map<String, Object> renderAsMap(Message message) {
        if (!matches(message)) {
            throw new IllegalStateException("Conditional template does not match");
        }
        return delegateTemplate.renderAsMap(message);
    }
}

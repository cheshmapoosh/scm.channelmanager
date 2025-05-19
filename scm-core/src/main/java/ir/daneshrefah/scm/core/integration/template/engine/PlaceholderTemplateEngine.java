package ir.daneshrefah.scm.core.integration.template.engine;

import ir.daneshrefah.scm.common.model.template.TemplateEngineType;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component("placeholder")
public class PlaceholderTemplateEngine implements TemplateEngine {

    @Override
    public TemplateEngineType getTemplateEngineType() {
        return TemplateEngineType.PLACEHOLDER;
    }

    @Override
    public String render(String name, String templateText, Map<String, Object> context) {
        // Convert Object values to String (StringSubstitutor expects String)
        Map<String, String> flatContext = context.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue() != null ? e.getValue().toString() : ""
            ));

        StringSubstitutor substitutor = new StringSubstitutor(flatContext);
        substitutor.setEnableSubstitutionInVariables(true); // supports nested vars
        return substitutor.replace(templateText);
    }
}

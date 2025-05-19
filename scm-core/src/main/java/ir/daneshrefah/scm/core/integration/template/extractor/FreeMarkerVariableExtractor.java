package ir.daneshrefah.scm.core.integration.template.extractor;

import ir.daneshrefah.scm.common.model.template.TemplateEngineType;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FreeMarkerVariableExtractor implements TemplateVariableExtractor {

    private static final Pattern FREEMARKER_VAR_PATTERN = Pattern.compile("\\$\\{\\s*([^}]+?)\\s*}");

    @Override
    public TemplateEngineType getTemplateEngineType() {
        return TemplateEngineType.FREEMARKER;
    }

    @Override
    public Set<String> extractVariables(String template) {
        Matcher matcher = FREEMARKER_VAR_PATTERN.matcher(template);
        Set<String> variables = new HashSet<>();
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables;
    }
}
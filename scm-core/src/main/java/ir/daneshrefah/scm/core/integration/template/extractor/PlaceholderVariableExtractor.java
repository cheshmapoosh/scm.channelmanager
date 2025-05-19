package ir.daneshrefah.scm.core.integration.template.extractor;

import ir.daneshrefah.scm.common.model.template.TemplateEngineType;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PlaceholderVariableExtractor implements TemplateVariableExtractor {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    @Override
    public TemplateEngineType getTemplateEngineType() {
        return TemplateEngineType.PLACEHOLDER;
    }

    @Override
    public Set<String> extractVariables(String template) {
        Set<String> vars = new HashSet<>();
        Matcher matcher = VAR_PATTERN.matcher(template);
        while (matcher.find()) {
            vars.add(matcher.group(1));
        }
        return vars;
    }
}

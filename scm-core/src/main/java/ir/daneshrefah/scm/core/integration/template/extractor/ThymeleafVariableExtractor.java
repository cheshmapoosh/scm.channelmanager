package ir.daneshrefah.scm.core.integration.template.extractor;

import ir.daneshrefah.scm.common.model.template.TemplateEngineType;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ThymeleafVariableExtractor implements TemplateVariableExtractor {

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\$\\{([^}]+)}|#\\{([^}]+)}");

    @Override
    public TemplateEngineType getTemplateEngineType() {
        return TemplateEngineType.THYMELEAF;
    }

    @Override
    public Set<String> extractVariables(String template) {
        Matcher matcher = EXPRESSION_PATTERN.matcher(template);
        Set<String> variables = new HashSet<>();
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                variables.add(matcher.group(1));
            } else if (matcher.group(2) != null) {
                variables.add("msg." + matcher.group(2));
            }
        }
        return variables;
    }
}
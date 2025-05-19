package ir.daneshrefah.scm.core.integration.template.context;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class I18nContextResolver implements ContextValueResolver {
    @Override
    public boolean supports(String key) {
        return key.startsWith("i18n.");
    }

    @Override
    public Object resolve(String key, Exchange exchange) {
        String code = key.substring("i18n.".length());
        Locale locale = Locale.getDefault(); // or resolve from header
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        return bundle.getString(code);
    }
}
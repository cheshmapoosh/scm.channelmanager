package ir.daneshrefah.scm.core.integration.template.context;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class I18nContextResolver implements ContextValueResolver {
    @Override
    public boolean supports(String key) {
        return StringUtils.startsWithIgnoreCase(key, "i18n.");
    }

    @Override
    public Object resolve(String key, Exchange exchange) {
        String code = StringUtils.removeStartIgnoreCase(key, "i18n.");
        Locale locale = Locale.getDefault(); // or resolve from header
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        return bundle.getString(code);
    }
}
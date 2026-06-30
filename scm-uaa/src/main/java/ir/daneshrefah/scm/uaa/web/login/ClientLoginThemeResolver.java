package ir.daneshrefah.scm.uaa.web.login;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;

@Component
public class ClientLoginThemeResolver {
    private final ClientLoginThemeProperties properties;

    public ClientLoginThemeResolver(ClientLoginThemeProperties properties) {
        this.properties = properties;
    }

    public LoginTheme resolve(String clientId) {
        Map<String, LoginTheme> clients = properties.getClients();
        if (StringUtils.hasText(clientId)) {
            LoginTheme exact = clients.get(clientId);
            if (exact != null) {
                return exact;
            }
            LoginTheme normalized = clients.get(clientId.trim().toLowerCase(Locale.ROOT));
            if (normalized != null) {
                return normalized;
            }
        }
        LoginTheme configuredDefault = clients.get(properties.getDefaultTheme());
        return configuredDefault == null ? LoginTheme.defaultTheme() : configuredDefault;
    }
}

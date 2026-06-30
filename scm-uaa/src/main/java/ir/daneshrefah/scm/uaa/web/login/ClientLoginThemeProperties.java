package ir.daneshrefah.scm.uaa.web.login;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "scm.uaa.login.theme")
public class ClientLoginThemeProperties {
    private String defaultTheme = "default";
    private Map<String, LoginTheme> clients = new LinkedHashMap<>();
}

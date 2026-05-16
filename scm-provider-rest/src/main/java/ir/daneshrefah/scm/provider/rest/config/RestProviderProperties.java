package ir.daneshrefah.scm.provider.rest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "scm.provider.rest")
public class RestProviderProperties {
    private boolean enabled = true;
    private Instance defaults = new Instance();
    private Map<String, Instance> providers = new HashMap<>();

    @Getter
    @Setter
    public static class Instance {
        private String baseUrl;
        private Integer connectTimeoutMs = 3000;
        private Integer responseTimeoutMs = 6000;
        private Boolean virtualThreadsEnabled = true;
        private Boolean insecureSsl = false;
        private String followRedirects = "NORMAL";
        private String defaultMethod = "POST";
        private Map<String, String> headers = new HashMap<>();
        private Proxy proxy = new Proxy();
        private Auth auth = new Auth();
        private Security security = new Security();
        private Token token = new Token();
    }

    @Getter
    @Setter
    public static class Proxy {
        private String host;
        private Integer port;
        private String username;
        private String password;
    }

    @Getter
    @Setter
    public static class Auth {
        private String type = "NONE";
        private String headerName = "Authorization";
        private String prefix;
        private String token;
        private String username;
        private String password;
        private Boolean basicBase64 = true;
    }

    @Getter
    @Setter
    public static class Security {
        private List<String> sensitiveHeaders = new ArrayList<>(List.of(
                "authorization",
                "proxy-authorization",
                "cookie",
                "set-cookie",
                "x-api-key",
                "api-key"
        ));
        private List<String> sensitiveBodyKeys = new ArrayList<>(List.of(
                "password",
                "token",
                "secret",
                "pin",
                "cvv",
                "pan",
                "card",
                "authorization"
        ));
        private Integer maxBodyLogLength = 400;
    }

    @Getter
    @Setter
    public static class Token {
        private Boolean enabled = false;
        private String cacheName = "rest_provider_token_cache";
        private String cacheKey = "access-token";
        private String lockName = "rest-provider-token";
        private Integer earlyRefreshSeconds = 30;
        private Integer defaultExpiresInSeconds = 300;
        private String method = "POST";
        private String url;
        private String path;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> query = new HashMap<>();
        private Map<String, String> form = new HashMap<>();
        private Map<String, Object> body = new HashMap<>();
        private Auth auth = new Auth();
        private String responseTokenField = "access_token";
        private String responseExpiresInField = "expires_in";
        private String responseTokenTypeField = "token_type";
        private String defaultTokenType = "Bearer";
    }
}

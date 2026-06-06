package ir.daneshrefah.scm.provider.rest.customizer;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Getter
@Setter
public class RestAuthUrlProviderMessageCustomizerConfig {
    private String url;
    private String path;
    private String method = "POST";
    private Request request = new Request();
    private Response response = new Response();
    private Cache cache = new Cache();
    private Lock lock = new Lock();
    private Apply apply = new Apply();

    public void validate(String providerCode) {
        if (StringUtils.isBlank(url) && StringUtils.isBlank(path)) {
            throw new IllegalArgumentException("rest-auth-url.url or rest-auth-url.path is required for provider " + providerCode);
        }
        cache.validate(providerCode);
        lock.validate(providerCode);
        apply.validate(providerCode);
    }

    public Request request() {
        return request == null ? new Request() : request;
    }

    public Response response() {
        return response == null ? new Response() : response;
    }

    public Cache cache() {
        return cache == null ? new Cache() : cache;
    }

    public Lock lock() {
        return lock == null ? new Lock() : lock;
    }

    public Apply apply() {
        return apply == null ? new Apply() : apply;
    }

    public RestAuthUrlProviderMessageCustomizerConfig copy() {
        RestAuthUrlProviderMessageCustomizerConfig target = new RestAuthUrlProviderMessageCustomizerConfig();
        target.url = url;
        target.path = path;
        target.method = method;
        target.request = request().copy();
        target.response = response().copy();
        target.cache = cache().copy();
        target.lock = lock().copy();
        target.apply = apply().copy();
        return target;
    }

    @Getter
    @Setter
    public static class Request {
        private Map<String, String> headers = new LinkedHashMap<>();
        private Map<String, String> query = new LinkedHashMap<>();
        private Map<String, String> form = new LinkedHashMap<>();
        private Map<String, Object> body = new LinkedHashMap<>();
        private Auth auth = new Auth();

        public Auth auth() {
            return auth == null ? new Auth() : auth;
        }

        Request copy() {
            Request target = new Request();
            target.headers = headers == null ? new LinkedHashMap<>() : new LinkedHashMap<>(headers);
            target.query = query == null ? new LinkedHashMap<>() : new LinkedHashMap<>(query);
            target.form = form == null ? new LinkedHashMap<>() : new LinkedHashMap<>(form);
            target.body = body == null ? new LinkedHashMap<>() : new LinkedHashMap<>(body);
            target.auth = auth().copy();
            return target;
        }
    }

    @Getter
    @Setter
    public static class Auth {
        private String type = "NONE";
        private String username;
        private String password;
        private String token;
        private String headerName = "Authorization";
        private String prefix;
        private Boolean basicBase64 = true;

        public AuthType authType() {
            String resolved = StringUtils.defaultIfBlank(type, "NONE").trim().toUpperCase(Locale.ROOT);
            try {
                return AuthType.valueOf(resolved);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unsupported rest-auth-url request.auth.type: " + type);
            }
        }

        Auth copy() {
            Auth target = new Auth();
            target.type = type;
            target.username = username;
            target.password = password;
            target.token = token;
            target.headerName = headerName;
            target.prefix = prefix;
            target.basicBase64 = basicBase64;
            return target;
        }
    }

    public enum AuthType {
        BASIC,
        BEARER,
        NONE
    }

    @Getter
    @Setter
    public static class Response {
        private String tokenField = "access_token";
        private String expiresInField = "expires_in";
        private String tokenTypeField = "token_type";
        private String defaultTokenType = "Bearer";
        private Integer defaultExpiresInSeconds = 300;

        Response copy() {
            Response target = new Response();
            target.tokenField = tokenField;
            target.expiresInField = expiresInField;
            target.tokenTypeField = tokenTypeField;
            target.defaultTokenType = defaultTokenType;
            target.defaultExpiresInSeconds = defaultExpiresInSeconds;
            return target;
        }
    }

    @Getter
    @Setter
    public static class Cache {
        private String name;
        private String keyPrefix = "provider-token";
        private String authProfile;
        private String credentialKey;
        private Object refreshSkew = "60s";
        private Object ttlSkew = "5s";

        public void validate(String providerCode) {
            if (StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("rest-auth-url.cache.name is required for provider " + providerCode);
            }
            if (StringUtils.isBlank(authProfile)) {
                throw new IllegalArgumentException("rest-auth-url.cache.auth-profile is required for provider " + providerCode);
            }
            if (StringUtils.isBlank(credentialKey)) {
                throw new IllegalArgumentException("rest-auth-url.cache.credential-key is required for provider " + providerCode);
            }
        }

        public Duration refreshSkewDuration() {
            return duration(refreshSkew, Duration.ofSeconds(60));
        }

        public Duration ttlSkewDuration() {
            return duration(ttlSkew, Duration.ofSeconds(5));
        }

        Cache copy() {
            Cache target = new Cache();
            target.name = name;
            target.keyPrefix = keyPrefix;
            target.authProfile = authProfile;
            target.credentialKey = credentialKey;
            target.refreshSkew = refreshSkew;
            target.ttlSkew = ttlSkew;
            return target;
        }
    }

    @Getter
    @Setter
    public static class Lock {
        private String keyPrefix = "provider-token-refresh-lock";
        private Object waitTimeout = "3s";
        private Object retryDelay = "100ms";

        public void validate(String providerCode) {
            if (StringUtils.isBlank(keyPrefix)) {
                throw new IllegalArgumentException("rest-auth-url.lock.key-prefix is required for provider " + providerCode);
            }
        }

        public Duration waitTimeoutDuration() {
            return duration(waitTimeout, Duration.ofSeconds(3));
        }

        public Duration retryDelayDuration() {
            return duration(retryDelay, Duration.ofMillis(100));
        }

        Lock copy() {
            Lock target = new Lock();
            target.keyPrefix = keyPrefix;
            target.waitTimeout = waitTimeout;
            target.retryDelay = retryDelay;
            return target;
        }
    }

    @Getter
    @Setter
    public static class Apply {
        private String location = "header";
        private String name;
        private String format = "{tokenType} {accessToken}";

        public void validate(String providerCode) {
            if (StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("rest-auth-url.apply.name is required for provider " + providerCode);
            }
            locationType();
        }

        public ApplyLocation locationType() {
            String resolved = StringUtils.defaultIfBlank(location, "header").trim().toUpperCase(Locale.ROOT);
            try {
                return ApplyLocation.valueOf(resolved);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unsupported rest-auth-url.apply.location: " + location);
            }
        }

        Apply copy() {
            Apply target = new Apply();
            target.location = location;
            target.name = name;
            target.format = format;
            return target;
        }
    }

    public enum ApplyLocation {
        HEADER,
        BODY,
        QUERY
    }

    static Duration duration(Object source, Duration fallback) {
        if (source == null) {
            return fallback;
        }
        if (source instanceof Duration duration) {
            return duration;
        }
        if (source instanceof Number number) {
            return Duration.ofMillis(number.longValue());
        }
        String text = StringUtils.trimToNull(String.valueOf(source));
        if (text == null) {
            return fallback;
        }
        try {
            return Duration.parse(text);
        } catch (Exception ignored) {
            // Continue with SCM-style compact values below, e.g. 60s or 100ms.
        }
        String normalized = text.toLowerCase(Locale.ROOT);
        try {
            if (normalized.endsWith("ms")) {
                return Duration.ofMillis(Long.parseLong(normalized.substring(0, normalized.length() - 2).trim()));
            }
            if (normalized.endsWith("s")) {
                return Duration.ofSeconds(Long.parseLong(normalized.substring(0, normalized.length() - 1).trim()));
            }
            if (normalized.endsWith("m")) {
                return Duration.ofMinutes(Long.parseLong(normalized.substring(0, normalized.length() - 1).trim()));
            }
            if (normalized.endsWith("h")) {
                return Duration.ofHours(Long.parseLong(normalized.substring(0, normalized.length() - 1).trim()));
            }
            return Duration.ofMillis(Long.parseLong(normalized));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid duration value: " + source, e);
        }
    }
}

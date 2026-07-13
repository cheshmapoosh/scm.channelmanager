package ir.daneshrefah.scm.web.observation.security;

import ir.daneshrefah.scm.core.integration.observability.attributes.CoreTraceAttributes;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class JwtGatewayAuthenticationTraceContributor implements GatewayAuthenticationTraceContributor {
    private static final String SUBJECT = "sub";
    private static final String SUBJECT_ID = "pid";
    private static final String ISSUER = "iss";
    private static final String AUDIENCE = "aud";
    private static final String SCOPE = "scope";
    private static final String AUTHORIZED_PARTY = "azp";
    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_ACCEPT_ADDRESS = "acp";
    private static final String LOGIN_METHOD = "lam";
    private static final String TRANSACTION_METHOD = "tam";

    @Override
    public boolean supports(Authentication authentication) {
        return validatedJwt(authentication) != null;
    }

    @Override
    public Map<String, Object> attributes(Authentication authentication) {
        Jwt jwt = validatedJwt(authentication);
        if (jwt == null) {
            return Map.of();
        }
        Map<String, Object> attributes = new LinkedHashMap<>();
        put(attributes, CoreTraceAttributes.AUTH_TYPE.name(), "bearer");
        put(attributes, CoreTraceAttributes.AUTH_SCHEME.name(), "jwt");
        put(attributes, CoreTraceAttributes.AUTH_SUBJECT_USERNAME.name(), stringClaim(jwt, SUBJECT));
        put(attributes, CoreTraceAttributes.AUTH_SUBJECT_ID.name(), stringClaim(jwt, SUBJECT_ID));
        put(attributes, CoreTraceAttributes.AUTH_ISSUER.name(), stringClaim(jwt, ISSUER));
        put(attributes, CoreTraceAttributes.AUTH_AUDIENCE.name(), audienceClaim(jwt));
        put(attributes, CoreTraceAttributes.AUTH_SCOPES.name(), scopeClaim(jwt));
        put(attributes, CoreTraceAttributes.AUTH_CLIENT_ID.name(), firstText(
                stringClaim(jwt, AUTHORIZED_PARTY),
                stringClaim(jwt, CLIENT_ID)
        ));
        put(attributes, CoreTraceAttributes.AUTH_CLIENT_ACCEPT_ADDRESS.name(),
                stringClaim(jwt, CLIENT_ACCEPT_ADDRESS));
        put(attributes, CoreTraceAttributes.AUTH_LOGIN_METHOD.name(), stringClaim(jwt, LOGIN_METHOD));
        put(attributes, CoreTraceAttributes.AUTH_TRANSACTION_METHOD.name(), stringClaim(jwt, TRANSACTION_METHOD));
        return Map.copyOf(attributes);
    }

    private Jwt validatedJwt(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return jwtAuthenticationToken.getToken();
        }
        if (authentication instanceof UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                && usernamePasswordAuthenticationToken.getCredentials() instanceof Jwt jwt) {
            return jwt;
        }
        if (authentication instanceof UserAuthentication userAuthentication
                && userAuthentication.getDetails() != null
                && userAuthentication.getDetails().getLoginData() instanceof Jwt jwt) {
            return jwt;
        }
        return null;
    }

    private String stringClaim(Jwt jwt, String claimName) {
        try {
            return trimmedString(jwt.getClaim(claimName));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private List<String> scopeClaim(Jwt jwt) {
        try {
            return normalizedStrings(jwt.getClaim(SCOPE), true);
        } catch (RuntimeException ignored) {
            return List.of();
        }
    }

    private List<String> audienceClaim(Jwt jwt) {
        try {
            List<String> values = normalizedStrings(jwt.getClaim(AUDIENCE), false);
            if (!values.isEmpty()) {
                return values;
            }
            return jwt.getAudience() == null ? List.of() : normalizedStrings(jwt.getAudience(), false);
        } catch (RuntimeException ignored) {
            return List.of();
        }
    }

    private String trimmedString(Object value) {
        if (value instanceof CharSequence text) {
            return textOrNull(text.toString());
        }
        if (value instanceof URI || value instanceof URL) {
            return textOrNull(value.toString());
        }
        return null;
    }

    private List<String> normalizedStrings(Object value, boolean splitWhitespace) {
        Set<String> values = new LinkedHashSet<>();
        appendStrings(values, value, splitWhitespace);
        return values.isEmpty() ? List.of() : List.copyOf(values);
    }

    private void appendStrings(Set<String> values, Object value, boolean splitWhitespace) {
        if (value == null) {
            return;
        }
        if (value instanceof Collection<?> collection) {
            collection.forEach(item -> appendStrings(values, item, splitWhitespace));
            return;
        }
        if (value.getClass().isArray()) {
            for (int index = 0; index < Array.getLength(value); index++) {
                appendStrings(values, Array.get(value, index), splitWhitespace);
            }
            return;
        }
        if (!(value instanceof CharSequence text)) {
            return;
        }
        String normalized = textOrNull(text.toString());
        if (normalized == null) {
            return;
        }
        if (!splitWhitespace) {
            values.add(normalized);
            return;
        }
        for (String item : normalized.split("\\s+")) {
            String candidate = textOrNull(item);
            if (candidate != null) {
                values.add(candidate);
            }
        }
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String text = textOrNull(value);
            if (text != null) {
                return text;
            }
        }
        return null;
    }

    private String textOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void put(Map<String, Object> attributes, String name, Object value) {
        if (name == null || name.isBlank() || value == null) {
            return;
        }
        if (value instanceof Collection<?> collection && collection.isEmpty()) {
            return;
        }
        attributes.put(name, value);
    }
}

package ir.daneshrefah.scm.web.observation.security;

import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URL;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class SpringSecurityGatewayJwtTraceContextResolver implements GatewayJwtTraceContextResolver {
    private static final String SUBJECT = "sub";
    private static final String SCOPE = "scope";
    private static final String ISSUER = "iss";
    private static final String CLIENT_ADDRESS = "acp";
    private static final String ISSUED_AT = "iat";
    private static final String EXPIRES_AT = "exp";
    private static final String CHANNEL_CODE = "trm";
    private static final String AUDIENCE = "aud";
    private static final String GENERATOR = "grn";
    private static final String TRANSACTION_METHOD = "tam";
    private static final String LOGIN_METHOD = "lam";

    @Override
    public GatewayJwtTraceContext resolve(Authentication authentication) {
        Jwt jwt;
        try {
            jwt = validatedJwt(authentication);
        } catch (RuntimeException ignored) {
            return GatewayJwtTraceContext.empty();
        }
        if (jwt == null) {
            return GatewayJwtTraceContext.empty();
        }
        return new GatewayJwtTraceContext(
                stringClaim(jwt, SUBJECT),
                scopeClaim(jwt, SCOPE),
                stringClaim(jwt, ISSUER),
                stringClaim(jwt, CLIENT_ADDRESS),
                instantClaim(jwt, ISSUED_AT),
                instantClaim(jwt, EXPIRES_AT),
                stringClaim(jwt, CHANNEL_CODE),
                audienceClaim(jwt, AUDIENCE),
                stringClaim(jwt, GENERATOR),
                stringClaim(jwt, TRANSACTION_METHOD),
                stringClaim(jwt, LOGIN_METHOD)
        );
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

    private List<String> scopeClaim(Jwt jwt, String claimName) {
        try {
            return normalizedStrings(jwt.getClaim(claimName), true);
        } catch (RuntimeException ignored) {
            return List.of();
        }
    }

    private List<String> audienceClaim(Jwt jwt, String claimName) {
        try {
            return normalizedStrings(jwt.getClaim(claimName), false);
        } catch (RuntimeException ignored) {
            return List.of();
        }
    }

    private Instant instantClaim(Jwt jwt, String claimName) {
        try {
            return normalizedInstant(jwt.getClaim(claimName));
        } catch (RuntimeException ignored) {
            return null;
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

    private Instant normalizedInstant(Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof Date date) {
            return date.toInstant();
        }
        BigDecimal numericDate = numericDate(value);
        if (numericDate == null) {
            return null;
        }
        try {
            BigInteger epochSecondValue = numericDate.setScale(0, RoundingMode.FLOOR).toBigIntegerExact();
            BigDecimal fractionalSecond = numericDate.subtract(new BigDecimal(epochSecondValue));
            long epochSecond = epochSecondValue.longValueExact();
            long nanoAdjustment = fractionalSecond.movePointRight(9)
                    .setScale(0, RoundingMode.DOWN)
                    .longValueExact();
            return Instant.ofEpochSecond(epochSecond, nanoAdjustment);
        } catch (ArithmeticException | DateTimeException ignored) {
            return null;
        }
    }

    private BigDecimal numericDate(Object value) {
        if (value instanceof Number number) {
            if ((number instanceof Double doubleValue && !Double.isFinite(doubleValue))
                    || (number instanceof Float floatValue && !Float.isFinite(floatValue))) {
                return null;
            }
            try {
                return new BigDecimal(number.toString());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (value instanceof CharSequence text) {
            String candidate = textOrNull(text.toString());
            if (candidate == null) {
                return null;
            }
            try {
                return new BigDecimal(candidate);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String textOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

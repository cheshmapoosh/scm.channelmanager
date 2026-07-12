package ir.daneshrefah.scm.core.integration.security;

import org.apache.camel.Exchange;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Keeps authenticated SCM business state local to a Camel Exchange and binds it only while one
 * processor is executing on the current thread.
 */
public final class ExchangeAuthenticationContext {
    public static final String AUTHENTICATION_PROPERTY = "scm.security.authentication";
    public static final String JWT_BUSINESS_CONTEXT_PROPERTY = "scm.security.jwt.business-context";

    private ExchangeAuthenticationContext() {
    }

    public static void store(
            Exchange exchange,
            Authentication authentication,
            ValidatedJwtBusinessContext jwtBusinessContext
    ) {
        if (exchange == null) {
            return;
        }
        if (authentication == null) {
            exchange.removeProperty(AUTHENTICATION_PROPERTY);
        } else {
            exchange.setProperty(AUTHENTICATION_PROPERTY, authentication);
        }
        ValidatedJwtBusinessContext safeContext = jwtBusinessContext == null
                ? ValidatedJwtBusinessContext.empty()
                : jwtBusinessContext;
        if (safeContext.roles().isEmpty()) {
            exchange.removeProperty(JWT_BUSINESS_CONTEXT_PROPERTY);
        } else {
            exchange.setProperty(JWT_BUSINESS_CONTEXT_PROPERTY, safeContext);
        }
    }

    public static Authentication authentication(Exchange exchange) {
        return exchange == null
                ? null
                : exchange.getProperty(AUTHENTICATION_PROPERTY, Authentication.class);
    }

    public static void clear(Exchange exchange) {
        if (exchange == null) {
            return;
        }
        exchange.removeProperty(AUTHENTICATION_PROPERTY);
        exchange.removeProperty(JWT_BUSINESS_CONTEXT_PROPERTY);
    }

    public static ValidatedJwtBusinessContext jwtBusinessContext(Exchange exchange) {
        ValidatedJwtBusinessContext context = exchange == null
                ? null
                : exchange.getProperty(JWT_BUSINESS_CONTEXT_PROPERTY, ValidatedJwtBusinessContext.class);
        return context == null ? ValidatedJwtBusinessContext.empty() : context;
    }

    /**
     * Opens a short same-thread binding for a single Camel processor invocation.
     */
    public static Binding bind(Exchange exchange) {
        SecurityContext previous = SecurityContextHolder.getContext();
        Authentication exchangeAuthentication = authentication(exchange);
        Authentication authentication = exchangeAuthentication;
        if (authentication == null && hasServletRequest(exchange)) {
            // Spring Security owns this request-thread authentication and clears it at filter completion.
            authentication = previous == null ? null : previous.getAuthentication();
        }

        SecurityContext local = SecurityContextHolder.createEmptyContext();
        local.setAuthentication(authentication);
        SecurityContextHolder.setContext(local);
        return new Binding(previous);
    }

    /**
     * Isolates a third-party authentication call which mutates SecurityContextHolder internally.
     */
    public static Binding isolateCurrentThread() {
        SecurityContext previous = SecurityContextHolder.getContext();
        SecurityContext local = SecurityContextHolder.createEmptyContext();
        local.setAuthentication(previous == null ? null : previous.getAuthentication());
        SecurityContextHolder.setContext(local);
        return new Binding(previous);
    }

    private static boolean hasServletRequest(Exchange exchange) {
        return exchange != null
                && exchange.getMessage() != null
                && exchange.getMessage().getHeader(Exchange.HTTP_SERVLET_REQUEST) != null;
    }

    public static final class Binding implements AutoCloseable {
        private final SecurityContext previous;
        private boolean closed;

        private Binding(SecurityContext previous) {
            this.previous = previous;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            if (previous == null) {
                SecurityContextHolder.clearContext();
            } else {
                SecurityContextHolder.setContext(previous);
            }
        }
    }
}

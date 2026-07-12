package ir.daneshrefah.scm.core.integration.security;

import java.util.List;

/**
 * Minimal immutable JWT-derived state required by SCM business authorization.
 *
 * <p>The compact token, Spring {@code Jwt}, headers, and unrestricted claims are deliberately excluded.</p>
 */
public record ValidatedJwtBusinessContext(List<String> roles) {
    private static final ValidatedJwtBusinessContext EMPTY = new ValidatedJwtBusinessContext(List.of());

    public ValidatedJwtBusinessContext {
        roles = roles == null ? List.of() : List.copyOf(roles);
    }

    public static ValidatedJwtBusinessContext empty() {
        return EMPTY;
    }
}

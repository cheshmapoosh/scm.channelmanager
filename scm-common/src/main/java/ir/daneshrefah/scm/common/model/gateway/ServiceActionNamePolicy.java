package ir.daneshrefah.scm.common.model.gateway;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Canonical naming policy for stateless service actions.
 */
public final class ServiceActionNamePolicy {
    public static final int MAX_LENGTH = 100;
    public static final String FORMAT = "[a-z][a-z0-9-]*(\\.[a-z][a-z0-9-]*)*";

    private static final Pattern VALID_ACTION = Pattern.compile(FORMAT);

    private ServiceActionNamePolicy() {
    }

    public static String canonicalize(String actionName) {
        if (actionName == null) {
            throw new IllegalArgumentException("Action name is required.");
        }
        String canonical = actionName.trim().toLowerCase(Locale.ROOT);
        if (canonical.isEmpty()) {
            throw new IllegalArgumentException("Action name is required.");
        }
        if (canonical.length() > MAX_LENGTH || !VALID_ACTION.matcher(canonical).matches()) {
            throw new IllegalArgumentException("Action name must match " + FORMAT
                    + " and contain at most " + MAX_LENGTH + " characters.");
        }
        return canonical;
    }
}

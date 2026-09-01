package ir.daneshrefah.scm.common.model.gateway;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Canonical naming policy for definition-owned inbound actions.
 */
public final class InboundActionPolicy {
    public static final int MAX_LENGTH = 100;
    public static final String FORMAT = "[a-z][a-z0-9-]*(\\.[a-z][a-z0-9-]*)*";

    private static final Pattern VALID_ACTION = Pattern.compile(FORMAT);

    private InboundActionPolicy() {
    }

    public static String canonicalize(String inboundAction) {
        if (inboundAction == null) {
            throw new IllegalArgumentException("Inbound action is required.");
        }
        String canonical = inboundAction.trim().toLowerCase(Locale.ROOT);
        if (canonical.isEmpty()) {
            throw new IllegalArgumentException("Inbound action is required.");
        }
        if (canonical.length() > MAX_LENGTH || !VALID_ACTION.matcher(canonical).matches()) {
            throw new IllegalArgumentException("Inbound action must match " + FORMAT
                    + " and contain at most " + MAX_LENGTH + " characters.");
        }
        return canonical;
    }
}

package ir.daneshrefah.scm.provider.scm.registry;

import java.util.regex.Pattern;

public final class ScmResourceNames {

    public static final String RESOURCE_NAME_EXPRESSION = "[a-z][a-z0-9-]*";
    public static final String ACTION_NAME_EXPRESSION =
            "[a-z][a-z0-9-]*(\\.[a-z][a-z0-9-]*)*";

    private static final Pattern RESOURCE_NAME = Pattern.compile(RESOURCE_NAME_EXPRESSION);
    private static final Pattern ACTION_NAME = Pattern.compile(ACTION_NAME_EXPRESSION);

    private ScmResourceNames() {
    }

    public static String requireResourceName(String value, String subject) {
        return requireName(value, subject, RESOURCE_NAME);
    }

    public static String requireActionName(String value, String subject) {
        return requireName(value, subject, ACTION_NAME);
    }

    private static String requireName(String value, String subject, Pattern pattern) {
        if (value == null || value.isBlank() || !value.equals(value.trim()) || !pattern.matcher(value).matches()) {
            throw new IllegalStateException(subject + " must match " + pattern.pattern());
        }
        return value;
    }
}

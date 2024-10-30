package ir.daneshrefah.scm.common.constant;

import java.util.Arrays;

public enum ParameterAutoCompleteProperty {
    /**
     * Refer to parameter name
     */

    NAME;

    public static ParameterAutoCompleteProperty fromValue(String value) {
        return Arrays.stream(values())
                .filter(property -> property.name().equalsIgnoreCase(value))
                .findFirst().orElse(null);
    }
}

package ir.daneshrefah.scm.provider.nab.domain;

import java.util.Locale;

public enum NabPadding {
    RIGHT_SPACE,
    LEFT_SPACE,
    LEFT_ZERO,
    NONE;

    public static NabPadding from(String value) {
        if (value == null || value.isBlank()) {
            return RIGHT_SPACE;
        }
        return NabPadding.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}

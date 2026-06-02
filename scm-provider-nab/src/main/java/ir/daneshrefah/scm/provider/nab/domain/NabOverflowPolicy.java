package ir.daneshrefah.scm.provider.nab.domain;

import java.util.Locale;

public enum NabOverflowPolicy {
    ERROR,
    TRUNCATE;

    public static NabOverflowPolicy from(String value) {
        if (value == null || value.isBlank()) {
            return ERROR;
        }
        return NabOverflowPolicy.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}

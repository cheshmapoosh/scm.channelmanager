package ir.daneshrefah.scm.provider.nab.domain;

import java.util.Locale;

public enum NabProtocol {
    ATPS,
    ATPI,
    MIRS;

    public static NabProtocol from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("NAB command protocol is required");
        }
        return NabProtocol.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}

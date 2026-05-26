package ir.daneshrefah.scm.provider.nab.domain;

import java.util.Locale;

public enum NabFieldType {
    STRING,
    NUMBER,
    DECIMAL,
    BOOLEAN,
    DATE,
    DATETIME,
    RAW;

    public static NabFieldType from(String value) {
        if (value == null || value.isBlank()) {
            return STRING;
        }
        return NabFieldType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}

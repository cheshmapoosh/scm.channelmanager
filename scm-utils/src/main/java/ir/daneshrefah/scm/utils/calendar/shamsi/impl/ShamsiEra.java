package ir.daneshrefah.scm.utils.calendar.shamsi.impl;

import java.time.DateTimeException;
import java.time.chrono.Era;
import java.time.temporal.TemporalField;
import java.time.temporal.ValueRange;

import static java.time.temporal.ChronoField.ERA;

/**
 * @author dariush abdolahi
 * @version 1.0.0
 */
public enum ShamsiEra implements Era {

    /* Anno Hegira Shamsi */
    AHS;

    public static ShamsiEra of(int persianEra) {
        if (persianEra == 1) {
            return AHS;
        }
        throw new DateTimeException("Invalid era for shamsi: " + persianEra);
    }

    @Override
    public int getValue() {
        return 1;
    }

    @Override
    public ValueRange range(TemporalField field) {
        if (field == ERA) {
            return ValueRange.of(1, 1);
        }
        return Era.super.range(field);
    }

}

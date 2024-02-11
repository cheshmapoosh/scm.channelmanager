package ir.daneshrefah.scm.utils.calendar.shamsi.impl;

import java.time.DateTimeException;
import java.time.chrono.AbstractChronology;
import java.time.chrono.Era;
import java.time.temporal.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.time.temporal.ChronoField.YEAR;

public class ShamsiChronology extends AbstractChronology {

    public static final ShamsiChronology SHAMSI_CHRONOLOGY = new ShamsiChronology();

    private ShamsiChronology() {
    }

    public static ShamsiChronology getInstance() {
        return SHAMSI_CHRONOLOGY;
    }


    void checkValidValue(long value, TemporalField field) {
        Objects.requireNonNull(field, "field");
        if (!(field instanceof ChronoField cf)) {
            throw new DateTimeException("Parameter 'field' is not supported");
        }
        if (value < range(cf).getMinimum() || value > range(cf).getMaximum()) {
            throw new DateTimeException("Invalid value for " + field + ", valid values: " + range(cf));
        }
    }


    void checkDayOfYear(int year, int dayOfYear) {
        checkValidValue(year, YEAR);
        int maxDayOfYear = isLeapYear(year) ? 366 : 365;
        if (dayOfYear < 1 || dayOfYear > maxDayOfYear) {
            throw new DateTimeException("Invalid value for dayOfYear: " + dayOfYear + " ");
        }
    }


    @Override
    public String getId() {
        return "Shamsi";
    }


    @Override
    public String getCalendarType() {
        return "Shamsi";
    }


    @Override
    public ShamsiDate date(int prolepticYear, int month, int dayOfMonth) {
        return ShamsiDate.of(prolepticYear, month, dayOfMonth);
    }


    @Override
    public ShamsiDate dateYearDay(int prolepticYear, int dayOfYear) {
        checkDayOfYear(prolepticYear, dayOfYear);
        return ShamsiDate.of(prolepticYear, 1, 1).plusDays(dayOfYear - 1);
    }


    @Override
    public ShamsiDate dateEpochDay(long epochDay) {
        return ShamsiDate.ofEpochDay(epochDay);
    }


    @Override
    public ShamsiDate date(TemporalAccessor temporal) {
        if (temporal instanceof ShamsiDate) {
            return (ShamsiDate) temporal;
        }
        return ShamsiDate.ofJulianDays(JulianFields.JULIAN_DAY.getFrom(temporal));
    }


    @Override
    public boolean isLeapYear(long year) {
        checkValidValue(year, YEAR);
        return ShamsiDate.isLeapYear((int) year);
    }


    @Override
    public int prolepticYear(Era era, int yearOfEra) {
        if (!(era instanceof ShamsiEra)) {
            throw new ClassCastException("Era must be PersianEra");
        }
        return yearOfEra;
    }


    @Override
    public Era eraOf(int eraValue) {
        if (eraValue == 1) {
            return ShamsiEra.AHS;
        }
        throw new DateTimeException("invalid Persian era");
    }


    @Override
    public List<Era> eras() {
        return Arrays.asList(ShamsiEra.values());
    }


    @Override
    public ValueRange range(ChronoField field) {
        return switch (field) {
            case DAY_OF_MONTH -> ValueRange.of(1, 1, 29, 31);
            case DAY_OF_YEAR -> ValueRange.of(1, 1, 365, 366);
            case ALIGNED_WEEK_OF_MONTH -> ValueRange.of(1, 5);
            case YEAR, YEAR_OF_ERA -> ValueRange.of(1, 1999);
            case ERA -> ValueRange.of(1, 1);
            default -> field.range();
        };
    }
}

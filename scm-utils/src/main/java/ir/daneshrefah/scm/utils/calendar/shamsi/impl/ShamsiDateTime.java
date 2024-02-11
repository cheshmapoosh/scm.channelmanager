package ir.daneshrefah.scm.utils.calendar.shamsi.impl;

import ir.daneshrefah.scm.utils.calendar.shamsi.constant.ShamsiMonth;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.Objects;

public class ShamsiDateTime implements ChronoLocalDateTime<ShamsiDate> {

    public static final ShamsiDateTime MIN = new ShamsiDateTime(ShamsiDate.MIN, LocalTime.MIN);
    public static final ShamsiDateTime MAX = new ShamsiDateTime(ShamsiDate.MAX, LocalTime.MAX);
    private final ShamsiDate date;
    private final LocalTime time;


    private ShamsiDateTime(final ShamsiDate date, final LocalTime time) {
        this.date = Objects.requireNonNull(date, "date");
        this.time = Objects.requireNonNull(time, "time");
    }

    public static ShamsiDateTime now() {
        return new ShamsiDateTime(ShamsiDate.now(), LocalTime.now());
    }

    public static ShamsiDateTime of(final ShamsiDate date, final LocalTime time) {
        return new ShamsiDateTime(date, time);
    }

    public static ShamsiDateTime of(final int year, final ShamsiMonth month, final int dayOfMonth, final int hour,
                                    final int minute) {

        final ShamsiDate date = ShamsiDate.of(year, month, dayOfMonth);
        final LocalTime time = LocalTime.of(hour, minute);
        return new ShamsiDateTime(date, time);
    }

    public static ShamsiDateTime of(final int year, final ShamsiMonth month, final int dayOfMonth, final int hour,
                                    final int minute, final int second) {

        final ShamsiDate date = ShamsiDate.of(year, month, dayOfMonth);
        final LocalTime time = LocalTime.of(hour, minute, second);
        return new ShamsiDateTime(date, time);
    }

    public static ShamsiDateTime of(final int year, final ShamsiMonth month, final int dayOfMonth, final int hour,
                                    final int minute, final int second, final int nanoOfSecond) {

        final ShamsiDate date = ShamsiDate.of(year, month, dayOfMonth);
        final LocalTime time = LocalTime.of(hour, minute, second, nanoOfSecond);
        return new ShamsiDateTime(date, time);
    }

    public static ShamsiDateTime of(final int year, final int month, final int dayOfMonth, final int hour,
                                    final int minute) {

        final ShamsiDate date = ShamsiDate.of(year, month, dayOfMonth);
        final LocalTime time = LocalTime.of(hour, minute);
        return new ShamsiDateTime(date, time);
    }

    public static ShamsiDateTime of(final int year, final int month, final int dayOfMonth, final int hour,
                                    final int minute, final int second) {

        final ShamsiDate date = ShamsiDate.of(year, month, dayOfMonth);
        final LocalTime time = LocalTime.of(hour, minute, second);
        return new ShamsiDateTime(date, time);
    }

    public static ShamsiDateTime of(final int year, final int month, final int dayOfMonth, final int hour,
                                    final int minute, final int second, final int nanoOfSecond) {

        final ShamsiDate date = ShamsiDate.of(year, month, dayOfMonth);
        final LocalTime time = LocalTime.of(hour, minute, second, nanoOfSecond);
        return new ShamsiDateTime(date, time);
    }

    public static ShamsiDateTime fromGregorian(final LocalDateTime localDateTime) {
        Objects.requireNonNull(localDateTime, "localDateTime");
        return new ShamsiDateTime(ShamsiDate.fromGregorian(localDateTime.toLocalDate()), localDateTime.toLocalTime());
    }

    public static ShamsiDateTime parse(final CharSequence text) {
        return parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static ShamsiDateTime parse(final CharSequence text, final DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.withChronology(ShamsiChronology.getInstance()).parse(text, ShamsiDateTime::from);
    }

    public static ShamsiDateTime from(final TemporalAccessor temporal) {
        Objects.requireNonNull(temporal, "temporal");
        if (temporal instanceof ShamsiDateTime shamsiDateTime) {
            return shamsiDateTime;
        } else {
            try {
                final ShamsiDate date = ShamsiDate.from(temporal);
                final LocalTime time = LocalTime.from(temporal);
                return new ShamsiDateTime(date, time);
            } catch (final DateTimeException ex) {
                throw new DateTimeException(
                        String.format("Unable to parse PersianDateTime from TemporalAccessor: %s", temporal), ex);
            }
        }
    }

    @Override
    public ShamsiDate toLocalDate() {
        return date;
    }


    @Override
    public LocalTime toLocalTime() {
        return time;
    }


    public LocalDateTime toGregorian() {
        return LocalDateTime.of(date.toGregorian(), time);
    }

    @Override
    public boolean isSupported(final TemporalField field) {
        return false;
    }


    @Override
    public long getLong(final TemporalField field) {
        Objects.requireNonNull(field, "field");
        if (field instanceof ChronoField chronoField) {
            return (chronoField.isTimeBased() ? time.getLong(field) : date.getLong(field));
        }
        return field.getFrom(this);
    }

    @Override
    public ChronoLocalDateTime<ShamsiDate> with(final TemporalField field, final long newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChronoLocalDateTime<ShamsiDate> plus(final long amountToAdd, final TemporalUnit temporalUnit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long until(final Temporal temporal, final TemporalUnit temporalUnit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChronoZonedDateTime<ShamsiDate> atZone(final ZoneId zoneId) {
        throw new UnsupportedOperationException();
    }


    @Override
    public int compareTo(final ChronoLocalDateTime<?> other) {
        int result = 0;
        if (other instanceof ShamsiDateTime otherPersianDateTime) {
            result = date.compareTo(otherPersianDateTime.date);
            if (result == 0) {
                result = time.compareTo(otherPersianDateTime.time);
            }
        }
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ShamsiDateTime) {
            return compareTo((ShamsiDateTime) obj) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time);
    }


    public String toString() {
        return String.format("%sT%s", date, time);
    }
}

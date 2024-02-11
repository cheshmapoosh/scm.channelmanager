package ir.daneshrefah.scm.utils.calendar.shamsi.impl;

import ir.daneshrefah.scm.utils.calendar.shamsi.constant.ShamsiMonth;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.Objects;

import static java.time.temporal.ChronoField.*;

/**
 * {@code
 * // Instantiate
 * ShamsiDate today = ShamsiDate.now();
 * ShamsiDate shamsiDate1 = ShamsiDate.of(1396, 7, 15);
 * ShamsiDate shamsiDate2 = ShamsiDate.of(1396, ShamsiMonth.MEHR, 15);
 *<br>
 * // Convert
 * ShamsiDate shamsiDate5 = ShamsiDate.of(1397, 5, 11);
 * LocalDate gregDate = ShamsiDate.toGregorian();    // => '2018-08-02'
 * ShamsiDate shamsiDate6 = ShamsiDate.fromGregorian(gregDate);  //  => '1397/05/11'
 *<br>
 * // Parse
 * ShamsiDate shamsiDate3 = ShamsiDate.parse("1400-06-15");    // From the standard format
 * ShamsiDate shamsiDate4 = ShamsiDate.parse("1400/06/15", DateTimeFormatter.ofPattern("yyyy/MM/dd"));    // From a desired format
 *<br>
 * // Format
 * DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
 * dtf.format(ShamsiDate.now());    // => e.g. '1396/05/10'
 * }
 */
public class ShamsiDate implements ChronoLocalDate {

    public static final ShamsiDate MIN = ShamsiDate.of((int) ShamsiChronology.getInstance().range(YEAR).getMinimum(), 1, 1);
    public static final ShamsiDate MAX = ShamsiDate.of((int) ShamsiChronology.getInstance().range(YEAR).getMaximum(), 12, 29);
    private static final long JULIAN_DAY_TO_1970 = 2440588L;
    private static final long CYCLE_DAYS = 1029983;
    private static final int CYCLE_YEARS = 2820;
    private static final double YEAR_LENGTH = 365.24219858156028368;
    private static final long SHAMSI_DATE_EPOCH = 2121446;
    private static final double LEAP_THRESHOLD = 0.24219858156028368;
    private final int year;
    private final int month;
    private final int day;


    private ShamsiDate(int year, int month, int dayOfMonth) {
        ShamsiChronology.getInstance().checkValidValue(year, YEAR);
        ShamsiChronology.getInstance().checkValidValue(month, MONTH_OF_YEAR);
        boolean leapYear = ShamsiChronology.getInstance().isLeapYear(year);
        int maxDaysOfMonth = ShamsiMonth.of(month).length(leapYear);
        if (dayOfMonth > maxDaysOfMonth) {
            if (month == 12 && dayOfMonth == 30 && !leapYear) {
                throw new DateTimeException("Invalid date ESFAND 30, as " + year + " is not a leap year");
            }
            throw new DateTimeException("Invalid date " + ShamsiMonth.of(month).name() + " " + dayOfMonth);
        }
        this.year = year;
        this.month = month;
        this.day = dayOfMonth;
    }

    public static ShamsiDate now() {
        return ofJulianDays(JulianFields.JULIAN_DAY.getFrom(LocalDate.now()));
    }

    public static ShamsiDate of(int year, int month, int dayOfMonth) {
        return new ShamsiDate(year, month, dayOfMonth);
    }

    public static ShamsiDate of(int year, ShamsiMonth month, int dayOfMonth) {
        Objects.requireNonNull(month, "month");
        return new ShamsiDate(year, month.getValue(), dayOfMonth);
    }

    public static ShamsiDate fromGregorian(LocalDate localDate) {
        Objects.requireNonNull(localDate, "localDate");
        return ofJulianDays(JulianFields.JULIAN_DAY.getFrom(localDate));
    }

    public static ShamsiDate parse(final CharSequence text) {
        return parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static ShamsiDate parse(final CharSequence text, final DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.withChronology(ShamsiChronology.getInstance()).parse(text, ShamsiDate::from);
    }

    public static ShamsiDate from(final TemporalAccessor temporal) {
        Objects.requireNonNull(temporal, "temporal");
        return ShamsiChronology.getInstance().date(temporal);
    }

    public static ShamsiDate ofEpochDay(long epochDays) {
        return ofJulianDays(epochDays + JULIAN_DAY_TO_1970);
    }

    public static ShamsiDate ofJulianDays(long julianDays) {
        final long offset = julianDays - SHAMSI_DATE_EPOCH;
        long cycle_no = offset / CYCLE_DAYS;
        if (offset < 0) {
            --cycle_no;
        }
        final long cycleStart = SHAMSI_DATE_EPOCH + cycle_no * CYCLE_DAYS;
        final int yc = (int) (Math.floor((julianDays - cycleStart) / YEAR_LENGTH));
        long year = yc + 475 + cycle_no * 2820;
        final long lll = SHAMSI_DATE_EPOCH + cycle_no * CYCLE_DAYS + (long) Math.floor((yc * YEAR_LENGTH));
        long day = julianDays - lll + 1;
        if (day > (isLeapYear((int) year) ? 366 : 365)) {
            year++;
            day = 1;
        }
        if (year <= 0) {
            year--;
        }
        int month;
        for (month = 1; month < 12; ++month) {
            if (day > ShamsiMonth.of(month).length(isLeapYear((int) year))) {
                day -= ShamsiMonth.of(month).length(isLeapYear((int) year));
            } else {
                break;
            }
        }
        return ShamsiDate.of((int) year, month, (int) day);
    }

    public static boolean isLeapYear(final int year) {
        if (year < 1) {
            throw new IllegalArgumentException();
        }
        return ((year + 2346) * LEAP_THRESHOLD % 1) < LEAP_THRESHOLD;
    }

    static long toJulianDay(int year, int month, int dayOfMonth) {

        long era = (year - 475) / CYCLE_YEARS;
        if ((year - 475) < 0) {
            era--;
        }
        final long y_c = (year - 475) - era * CYCLE_YEARS;
        final long f_d = SHAMSI_DATE_EPOCH + era * CYCLE_DAYS + (long) Math.floor((y_c * YEAR_LENGTH));
        return f_d + ShamsiMonth.of(month).daysToFirstOfMonth() + dayOfMonth - 1;
    }

    public int getYear() {
        return year;
    }

    public ShamsiMonth getMonth() {
        return ShamsiMonth.of(month);
    }

    public int getMonthValue() {
        return month;
    }

    public int getDayOfMonth() {
        return day;
    }

    public int getDayOfYear() {
        return ShamsiMonth.of(month).daysToFirstOfMonth() + day;
    }

    public DayOfWeek getDayOfWeek() {
        int dow0 = Math.floorMod((int) toEpochDay() + 3, 7);
        return DayOfWeek.of(dow0 + 1);
    }

    @Override
    public Chronology getChronology() {
        return ShamsiChronology.getInstance();
    }

    @Override
    public int lengthOfMonth() {
        ShamsiMonth pm = ShamsiMonth.of(month);
        return ShamsiChronology.getInstance().isLeapYear(year) ? pm.maxLength() : pm.minLength();
    }

    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        Objects.requireNonNull(endExclusive, "endExclusive");
        Objects.requireNonNull(unit, "unit");
        ShamsiDate end = (ShamsiDate) getChronology().date(endExclusive);
        if (unit instanceof ChronoUnit) {
            return switch ((ChronoUnit) unit) {
                case DAYS -> daysUntil(end);
                case WEEKS -> daysUntil(end) / 7;
                case MONTHS -> monthsUntil(end);
                case YEARS -> monthsUntil(end) / 12;
                case DECADES -> monthsUntil(end) / 120;
                case CENTURIES -> monthsUntil(end) / 1200;
                case MILLENNIA -> monthsUntil(end) / 12000;
                case ERAS -> end.getLong(ERA) - getLong(ERA);
                default -> throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
            };
        }
        return unit.between(this, end);
    }

    private long daysUntil(ShamsiDate end) {
        return end.toEpochDay() - toEpochDay();  // no overflow
    }

    private long monthsUntil(ShamsiDate end) {
        long packed1 = getLong(PROLEPTIC_MONTH) * 32L + getDayOfMonth();  // no overflow
        long packed2 = end.getLong(PROLEPTIC_MONTH) * 32L + end.getDayOfMonth();  // no overflow
        return (packed2 - packed1) / 32;
    }

    @Override
    public ChronoPeriod until(ChronoLocalDate endDateExclusive) {
        Objects.requireNonNull(endDateExclusive, "endDateExclusive");
        ShamsiDate end = ShamsiChronology.getInstance().date(endDateExclusive);
        long totalMonths = end.getLong(PROLEPTIC_MONTH) - this.getLong(PROLEPTIC_MONTH);  // safe
        int days = end.day - this.day;
        if (totalMonths > 0 && days < 0) {
            totalMonths--;
            ShamsiDate calcDate = this.plusMonths(totalMonths);
            days = (int) (end.toEpochDay() - calcDate.toEpochDay());  // safe
        } else if (totalMonths < 0 && days > 0) {
            totalMonths++;
            days -= end.lengthOfMonth();
        }
        long years = totalMonths / 12;  // safe
        int months = (int) (totalMonths % 12);  // safe
        return Period.of(Math.toIntExact(years), months, days);
    }

    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField chronoField) {
            return switch (chronoField) {
                case DAY_OF_WEEK -> getDayOfWeek().getValue();
                case ALIGNED_DAY_OF_WEEK_IN_MONTH -> ((day - 1) % 7) + 1;
                case ALIGNED_DAY_OF_WEEK_IN_YEAR -> ((getDayOfYear() - 1) % 7) + 1;
                case DAY_OF_MONTH -> this.day;
                case DAY_OF_YEAR -> this.getDayOfYear();
                case EPOCH_DAY -> this.toEpochDay();
                case ALIGNED_WEEK_OF_MONTH -> ((day - 1) / 7) + 1;
                case ALIGNED_WEEK_OF_YEAR -> ((getDayOfYear() - 1) / 7) + 1;
                case MONTH_OF_YEAR -> month;
                case PROLEPTIC_MONTH -> (year * 12L + month - 1);
                case YEAR_OF_ERA -> (year >= 1 ? year : 1 - year);
                case YEAR -> year;
                case ERA -> (year >= 1 ? 1 : 0);
                default -> throw new UnsupportedTemporalTypeException("Unsupported temporal Field: " + field);
            };
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    public ShamsiDate plusYears(long yearsToAdd) {
        return plusMonths(yearsToAdd * 12);
    }

    public ShamsiDate plusMonths(long monthsToAdd) {
        if (monthsToAdd == 0) {
            return this;
        }
        long monthCount = year * 12L + (month - 1);
        long calcMonths = monthCount + monthsToAdd;
        int newYear = (int) Math.floorDiv(calcMonths, 12L);
        int newMonth = (int) Math.floorMod(calcMonths, 12L) + 1;
        return resolvePreviousValid(newYear, newMonth, day);
    }

    public ShamsiDate plusDays(long daysToAdd) {
        if (daysToAdd == 0) {
            return this;
        }
        return ofJulianDays(toJulianDay() + daysToAdd);
    }

    @Override
    public boolean isLeapYear() {
        return isLeapYear(year);
    }

    private ShamsiDate resolvePreviousValid(int year, int month, int day) {
        boolean leapYear = ShamsiChronology.getInstance().isLeapYear(year);
        int maxDaysOfMonth = ShamsiMonth.of(month).length(leapYear);
        if (day > maxDaysOfMonth) {
            day = maxDaysOfMonth;
        }
        return ShamsiDate.of(year, month, day);
    }

    public LocalDate toGregorian() {
        return LocalDate.from(this);
    }

    @Override
    public long toEpochDay() {
        return toJulianDay() - JULIAN_DAY_TO_1970;
    }

    public long toJulianDay() {
        return toJulianDay(year, month, day);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ShamsiDate) {
            return compareTo((ShamsiDate) obj) == 0;
        }
        return false;
    }


    @Override
    public int hashCode() {
        return Objects.hash(year, month, day);
    }


    public String toString() {
        return String.format("%04d-%02d-%02d", year, month, day);
    }
}

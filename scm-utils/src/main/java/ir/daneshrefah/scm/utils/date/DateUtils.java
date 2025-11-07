package ir.daneshrefah.scm.utils.date;

import ir.daneshrefah.scm.utils.calendar.shamsi.constant.ShamsiMonth;
import ir.daneshrefah.scm.utils.calendar.shamsi.impl.ShamsiDate;
import ir.daneshrefah.scm.utils.calendar.shamsi.impl.ShamsiDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-31
 */
public class DateUtils {

    @Getter
    @RequiredArgsConstructor
    public static enum FixedFormat {
        /**
         * COMPACT time format: {@code "yyyyMMddHHmmssSSS"}.
         */
        COMPACT("yyyyMMddHHmmssSSS", "yyyyMMdd"),
        /**
         * DATE_AND_TIME time format: {@code "dd MMM yyyy HH:mm:ss,SSS"}.
         */
        DATE("dd MMM yyyy HH:mm:ss,SSS", "dd MMM yyyy "),
        /**
         * DEFAULT time format: {@code "yyyy-MM-dd HH:mm:ss,SSS"}.
         */
        DEFAULT("yyyy-MM-dd HH:mm:ss,SSS", "yyyy-MM-dd "),
        /**
         * DEFAULT time format with microsecond precision: {@code "yyyy-MM-dd HH:mm:ss,nnnnnn"}.
         */
        DEFAULT_MICROS("yyyy-MM-dd HH:mm:ss,nnnnnn", "yyyy-MM-dd "),
        /**
         * ISO8601_BASIC time format: {@code "yyyyMMdd'T'HHmmss,SSS"}.
         */
        ISO8601_BASIC("yyyyMMdd'T'HHmmss,SSS", "yyyyMMdd'T'"),
        /**
         * ISO8601_BASIC time format: {@code "yyyyMMdd'T'HHmmss.SSS"}.
         */
        ISO8601_BASIC_PERIOD("yyyyMMdd'T'HHmmss.SSS", "yyyyMMdd'T'"),
        /**
         * ISO8601 time format: {@code "yyyy-MM-dd'T'HH:mm:ss,SSS"}.
         */
        ISO8601("yyyy-MM-dd'T'HH:mm:ss,SSS", "yyyy-MM-dd'T'"),
        /**
         * ISO8601 time format: {@code "yyyy-MM-dd'T'HH:mm:ss,SSSX"} with a time zone like {@code -07}.
         */
        ISO8601_OFFSET_DATE_TIME_HH("yyyy-MM-dd'T'HH:mm:ss,SSSX", "yyyy-MM-dd'T'"),
        /**
         * ISO8601 time format: {@code "yyyy-MM-dd'T'HH:mm:ss,SSSXX"} with a time zone like {@code -0700}.
         */
        ISO8601_OFFSET_DATE_TIME_HHMM("yyyy-MM-dd'T'HH:mm:ss,SSSXX", "yyyy-MM-dd'T'"),
        /**
         * ISO8601 time format: {@code "yyyy-MM-dd'T'HH:mm:ss,SSSXXX"} with a time zone like {@code -07:00}.
         */
        ISO8601_OFFSET_DATE_TIME_HHCMM("yyyy-MM-dd'T'HH:mm:ss,SSSXXX", "yyyy-MM-dd'T'"),
        /**
         * ISO8601 time format: {@code "yyyy-MM-dd'T'HH:mm:ss.SSS"}.
         */
        ISO8601_PERIOD("yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'"),
        /**
         * ISO8601 time format with support for microsecond precision: {@code "yyyy-MM-dd'T'HH:mm:ss.nnnnnn"}.
         */
        ISO8601_PERIOD_MICROS("yyyy-MM-dd'T'HH:mm:ss.nnnnnn", "yyyy-MM-dd'T'"),
        /**
         * American date/time format with 2-digit year: {@code "dd/MM/yy HH:mm:ss.SSS"}.
         */
        US_MONTH_DAY_YEAR2_TIME("dd/MM/yy HH:mm:ss.SSS", "dd/MM/yy "),
        /**
         * American date/time format with 4-digit year: {@code "dd/MM/yyyy HH:mm:ss.SSS"}.
         */
        US_MONTH_DAY_YEAR4_TIME("dd/MM/yyyy HH:mm:ss.SSS", "dd/MM/yyyy ");

        private final String dateTimePattern;
        private final String datePattern;

    }
    public static class InstantTools {
        public static long calculateMinutesBetween(Instant fromDate, Instant toDate) {
            return fromDate.until(toDate, ChronoUnit.MINUTES);
        }

        public static long calculateSecondsBetween(Instant fromDate, Instant toDate) {
            return Duration.between(fromDate, toDate).getSeconds();
        }

        public static long calculateMillisBetween(Instant fromDate, Instant toDate) {
            return Duration.between(fromDate, toDate).toMillis();
        }

        public static long calculateSecondsFromNowToDate(Instant toDate) {
            return calculateSecondsBetween(currentDate(), toDate);
        }

        public static Instant currentDate() {
            return Instant.now();
        }

        public static Instant plusSeconds(Instant instant, long secondsToAdd) {
            return instant.plusSeconds(secondsToAdd);
        }

        public static Instant plusSecondsToCurrent(long secondsToAdd) {
            return currentDate().plusSeconds(secondsToAdd);
        }

        public static Instant plusMinutesToCurrent(long minutesToAdd) {
            return currentDate().plus(minutesToAdd, ChronoUnit.MINUTES);
        }

        public static Instant convertToInstant(String input) {
            // sample value: 2024-01-06T05:15:50.854476Z
            return Instant.parse(input);
        }
    }

    public static class TimestampTools{
        public static Timestamp getCurrentTimestamp(){
            return new Timestamp(System.currentTimeMillis());
        }
    }

    public static class LocalDateTimeTools {

        public static LocalDateTime getCurrentLocalDateTime(){
            return LocalDateTime.now();
        }

        public static LocalDateTime atStartOfDay(LocalDateTime localDateTime) {
            return localDateTime.toLocalDate().atStartOfDay();
        }

        public static LocalDateTime atStartOfDay(LocalDate localDate) {
            return localDate.atStartOfDay();
        }

        public static LocalDateTime atEndOfDay(LocalDate localDate) {
            return LocalTime.MAX.atDate(localDate);
        }

        public static LocalDateTime atEndOfDay(LocalDateTime localDateTime) {
            return localDateTime.with(ChronoField.NANO_OF_DAY, LocalTime.MAX.toNanoOfDay());
        }

        public static LocalDateTime current(){
            return LocalDateTime.now();
        }

        public static LocalDateTime plus(LocalDateTime dateTime,Duration duration){
            return dateTime.plus(duration);
        }
    }

    public static class ShamsiCalendarConvertor {

        public static ShamsiDate getCurrentDate(){
            return ShamsiDate.now();
        }

        public static ShamsiDateTime getCurrentDateTime(){
            return ShamsiDateTime.now();
        }

        public static ShamsiDate convertToShamsiCalendar(Timestamp timestamp){
            return convertToShamsiCalendar(DateConverter.convertToLocalDate(timestamp));
        }

        public static ShamsiDate convertToShamsiCalendar(LocalDate localDate){
            return ShamsiDate.fromGregorian(localDate);
        }

        public static ShamsiDate convertToShamsiCalendar(Date date){
             return convertToShamsiCalendar(DateConverter.convertToLocalDate(date));
        }

        public static String convertToShamsiDateString(LocalDate localDate, String pattern) {
            ShamsiDate shamsiDate = ShamsiDate.fromGregorian(localDate);
            return shamsiDate.format(DateTimeFormatter.ofPattern(pattern));
        }

        public static String convertToShamsiDateString(LocalDateTime localDateTime, String pattern) {
            ShamsiDateTime shamsiDate = ShamsiDateTime.fromGregorian(localDateTime);
            return shamsiDate.format(DateTimeFormatter.ofPattern(pattern));
        }

        public static String convertToShamsiDateString(Date date, String pattern) {
            return convertToShamsiDateString(DateUtils.DateConverter.convertToLocalDate(date),pattern);
        }

        public static String convertToShamsiDateString(Timestamp timestamp, String pattern) {
            return convertToShamsiDateString(DateUtils.DateConverter.convertToLocalDate(timestamp),pattern);
        }

        public static Timestamp convertToTimestamp(String shamsiDateString, String pattern) {
            return DateConverter.convertToTimestamp(convertToDate(shamsiDateString,pattern));
        }

        public static LocalDate convertCompactToLocalDate(String shamsiDateString) {
            return convertToLocalDate(shamsiDateString, FixedFormat.COMPACT.getDatePattern());
        }

        public static LocalDate convertToLocalDate(String shamsiDateString, String pattern) {
            ShamsiDate shamsiDate = ShamsiDate.parse(shamsiDateString, DateTimeFormatter.ofPattern(pattern));
            return shamsiDate.toGregorian();
        }

        public static LocalDateTime convertToLocalDateTime(String shamsiDateString, String pattern) {
            ShamsiDateTime shamsiDate = ShamsiDateTime.parse(shamsiDateString, DateTimeFormatter.ofPattern(pattern));
            return shamsiDate.toGregorian();
        }

        public static Date convertToDate(String shamsiDateString, String pattern) {
            return DateUtils.DateConverter.convertToDate(convertToLocalDateTime(shamsiDateString,pattern));
        }

    }

    public static class DateConverter {

        public static Date convertToDate(Timestamp timestamp) {
            Instant instant = convertToInstant(timestamp);
            return convertToDate(instant);
        }

        public static LocalDate convertToLocalDate(java.sql.Date sqlDate){
            return sqlDate.toLocalDate();
        }

        public static java.sql.Date convertToSqlDate(LocalDate localDate){
            return  java.sql.Date.valueOf(localDate);
        }

        public static Timestamp convertToTimestamp(Date date) {
            Instant instant = convertToInstant(date);
            return convertToTimestamp(instant);
        }

        public static LocalDateTime convertToLocalDateTime(Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }

        public static LocalDate convertToLocalDate(Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }

        public static Timestamp convertToTimestamp(LocalDateTime localDateTime) {
            return Timestamp.valueOf(localDateTime);
        }


        // Date - Instant
        public static Instant convertToInstant(Date dateToConvert) {
            return dateToConvert.toInstant();
        }

        public static Date convertToDate(Instant instantToConvert) {
            return Date.from(instantToConvert);
        }

        // Instant - Timestamp
        public static Instant convertToInstant(Timestamp timestamp) {
            return timestamp.toInstant();
        }

        public static Timestamp convertToTimestamp(Instant instant) {
            return Timestamp.from(instant);
        }

        // LocalDate - Date
        public static LocalDate convertToLocalDate(Date dateToConvert) {
            return dateToConvert.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        public static Date convertToDate(LocalDate localDateToConvert) {
            return java.util.Date.from(localDateToConvert.atStartOfDay()
                    .atZone(ZoneId.systemDefault())
                    .toInstant());
        }

        // LocalDateTime  - Date
        public static LocalDateTime convertToLocalDateTime(Date dateToConvert) {
            return dateToConvert.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }

        public static Date convertToDate(LocalDateTime localDateToConvert) {
            return java.util.Date
                    .from(localDateToConvert
                            .atZone(ZoneId.systemDefault())
                            .toInstant());
        }

        public static LocalDate convertToLocalDate(int year, int month, int day) {
            return LocalDate.of(year, month, day);
        }

        public static String convertToString(Date date, String pattern) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            return simpleDateFormat.format(date);
        }
    }

    public static class AgeCalculator {

        public static int getYear(int year, int month, int day) {
            LocalDate birthDate = LocalDate.of(year, month, day);
            return Period.between(birthDate, LocalDate.now()).getYears();
        }

        public static Period getAge(int year, int month, int day){
            LocalDate birthDate = LocalDate.of(year, month, day);
            return Period.between(birthDate, LocalDate.now());
        }

        public static int getYearByShamsiDate(int shamsiYear, ShamsiMonth shamsiMonth, int shamsiDay) {
            Timestamp timestamp = ShamsiCalendarConvertor.convertToTimestamp(shamsiYear + "-" + shamsiMonth.getValue() + "-" + shamsiDay, "yyyy-M-d");
            LocalDate birthDate = DateConverter.convertToLocalDate(timestamp);
            return Period.between(birthDate, LocalDate.now()).getYears();
        }

        public static Period getAgeByShamsiDate(int shamsiYear, ShamsiMonth shamsiMonth, int shamsiDay) {
            Timestamp timestamp = ShamsiCalendarConvertor.convertToTimestamp(shamsiYear + "-" + shamsiMonth.getValue() + "-" + shamsiDay, "yyyy-M-d");
            LocalDate birthDate = DateConverter.convertToLocalDate(timestamp);
            return Period.between(birthDate, LocalDate.now());
        }

    }


}

package ir.daneshrefah.scm.utils.date;

import ir.daneshrefah.scm.utils.calendar.shamsi.constant.ShamsiMonth;
import ir.daneshrefah.scm.utils.calendar.shamsi.impl.ShamsiDate;
import ir.daneshrefah.scm.utils.calendar.shamsi.impl.ShamsiDateTime;

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

    public static class InstantTools {
        public static long calculateMinutesBetween(Instant fromDate, Instant toDate) {
            return fromDate.until(toDate, ChronoUnit.MINUTES);
        }

        public static long calculateSecondsBetween(Instant fromDate, Instant toDate) {
            return Duration.between(fromDate, toDate).getSeconds();
        }

        public static long calculateSecondsFromNowToDate(Instant toDate) {
            return calculateSecondsBetween(currentDate(), toDate);
        }

        public static Instant currentDate() {
            return Instant.now();
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
    }

    public static class ShamsiCalendarConvertor {

        public static ShamsiDate getCurrentDate(){
            return ShamsiDate.now();
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

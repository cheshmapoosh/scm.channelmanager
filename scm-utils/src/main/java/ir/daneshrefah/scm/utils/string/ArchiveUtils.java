package ir.daneshrefah.scm.utils.string;

import ir.daneshrefah.scm.utils.date.DateUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
public class ArchiveUtils {

    public synchronized static Long calculateTenYearsYearlyArchiveNo() {
        return calculateTenYearsYearlyArchiveNo(new Date());
    }


    public synchronized static Long calculateTenYearsYearlyArchiveNo(Date date) {
        /* Gregorian year mod 10 */
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return (long) (calendar.get(Calendar.YEAR) % 10);
    }

    public synchronized static Long calculateTenDaysArchiveNo() {
        return calculateTenDaysArchiveNo(new Date());
    }

    public synchronized static Long calculateOneMonthArchiveNo() {
        return calculateOneMonthArchiveNo(new Date());
    }

    public synchronized static Long calculateOneYearArchiveNo() {
        return calculateOneYearArchiveNo(new Date());
    }

    public synchronized static Long calculateTwoYearsArchiveNo() {
        return calculateTwoYearsArchiveNo(new Date());
    }

    public synchronized static Long calculateTenYearsArchiveNo() {
        return calculateTenYearsArchiveNo(new Date());
    }

    public synchronized static Long calculateOneYearArchiveNo(Date date) {
        try {
            return (long) DateUtils.ShamsiCalendarConvertor.convertToShamsiCalendar(date).getDayOfYear();
        } catch (Exception ex) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            return (long) calendar.get(Calendar.DAY_OF_YEAR);
        }
    }

    public synchronized static Long calculateOneMonthArchiveNo(Date date) {
        return (long) DateUtils.ShamsiCalendarConvertor.convertToShamsiCalendar(date).getMonthValue();
    }

    public synchronized static Long calculateTwoYearsArchiveNo(Date date) {
        SimpleDateFormat ISO_EXPIRY_DATE_PATTERN = new SimpleDateFormat("yyMM");
        String yearMonth = ISO_EXPIRY_DATE_PATTERN.format(date);
        String year = yearMonth.substring(0, 2);
        String month = yearMonth.substring(2);
        return Long.valueOf(StringUtils.EMPTY + (Long.parseLong(year) % 2) + month);
    }

    public synchronized static Long calculateTenDaysArchiveNo(Date date) {
        final int BASE_YEAR = 1900;
        LocalDateTime localDateTime = LocalDateTime.of(BASE_YEAR, 1, 1, 0, 0, 0);
        long between = ChronoUnit.DAYS.between(localDateTime, DateUtils.DateConverter.convertToLocalDateTime(date));
        return between % 10;
    }

    public synchronized static Long calculateTenYearsArchiveNo(Date date) {
        final String SMALLEST_DATE_PATTERN = "yyMMdd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(SMALLEST_DATE_PATTERN);
        /* Fixed len 5 digit number
           1 digit : year module 10
           2 digits : month no
           2 digits : day no
         */
        String currentDate = dateFormat.format(date);
        return !currentDate.isBlank() && currentDate.length() >= 6 ?
                Long.valueOf(currentDate.substring(1)) :
                Long.valueOf("11111");
    }


}
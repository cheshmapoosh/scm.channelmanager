package ir.daneshrefah.scm.provider.nab.codec;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class PersianDateFormatter {

    public String nowTimestamp() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        int[] jalali = toJalali(now.toLocalDate());
        return "%04d%02d%02d%02d%02d%02d".formatted(
                jalali[0],
                jalali[1],
                jalali[2],
                now.getHour(),
                now.getMinute(),
                now.getSecond()
        );
    }

    private int[] toJalali(LocalDate date) {
        int gy = date.getYear() - 1600;
        int gm = date.getMonthValue() - 1;
        int gd = date.getDayOfMonth() - 1;

        int[] gdm = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int gDayNo = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400;
        for (int i = 0; i < gm; ++i) {
            gDayNo += gdm[i];
        }
        if (gm > 1 && isGregorianLeap(date.getYear())) {
            gDayNo++;
        }
        gDayNo += gd;

        int jDayNo = gDayNo - 79;
        int jNp = jDayNo / 12053;
        jDayNo %= 12053;

        int jy = 979 + 33 * jNp + 4 * (jDayNo / 1461);
        jDayNo %= 1461;

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365;
            jDayNo = (jDayNo - 1) % 365;
        }

        int[] jdm = {31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29};
        int jm = 0;
        while (jm < 11 && jDayNo >= jdm[jm]) {
            jDayNo -= jdm[jm];
            jm++;
        }
        return new int[]{jy, jm + 1, jDayNo + 1};
    }

    private boolean isGregorianLeap(int year) {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }
}

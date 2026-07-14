package ir.daneshrefah.scm.common.transformerUtil.converter;

import com.github.mfathi91.time.PersianDate;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@UtilityClass
public class DateAndTimeConverter {
    public Long convertMsToPersianDate(Long millis) {
        if (Objects.isNull(millis) || millis.equals(0L)) {
            return null;
        }
        System.out.println(millis);
        LocalDate date = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        PersianDate persianDate = PersianDate.fromGregorian(date);

        System.out.println(persianDate);

        return Long.valueOf(persianDate.toString().replace("-", ""));
    }

    public Long convertPersianDateToMs(String persianDate) {
        if (Objects.isNull(persianDate) ||  persianDate.isEmpty()) {
            return null;
        }

        persianDate = persianDate.replace("-", "");

        int year = Integer.parseInt(persianDate.substring(0, 4));
        int month = Integer.parseInt(persianDate.substring(4, 6));
        int day = Integer.parseInt(persianDate.substring(6, 8));

        PersianDate pDate = PersianDate.of(year, month, day);

        LocalDate gregorianDate = pDate.toGregorian();

        long millis = gregorianDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        return millis;
    }

    public static void main(String[] args) {
        convertPersianDateToMs("14050417");
//        convertMsToPersianDate(System.currentTimeMillis());
    }
}

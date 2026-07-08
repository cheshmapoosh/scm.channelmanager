package ir.daneshrefah.scm.common.transformerUtil.converter;

import com.github.mfathi91.time.PersianDate;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

@UtilityClass
public class DateAndTimeConverter {
    public PersianDate convertMsToPersianDate(Long millis){
        System.out.println(millis);
        LocalDate date = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        PersianDate persianDate = PersianDate.fromGregorian(date);

        System.out.println(persianDate);

        return persianDate;
    }

    public Long convertPersianDateToMs(String persianDate){
        persianDate = persianDate.replace("-", "");

        int year = Integer.parseInt(persianDate.substring(0, 4));
        int month = Integer.parseInt(persianDate.substring(4, 6));
        int day = Integer.parseInt(persianDate.substring(6, 8));

        PersianDate pDate = PersianDate.of(year, month, day);

        long millis = pDate
                .toGregorian()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        System.out.println(millis);

        return millis;
    }

    public static void main(String[] args) {
        convertPersianDateToMs("14050417");
//        convertMsToPersianDate(System.currentTimeMillis());
    }
}

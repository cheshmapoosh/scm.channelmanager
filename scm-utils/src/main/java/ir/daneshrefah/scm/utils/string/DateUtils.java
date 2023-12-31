package ir.daneshrefah.scm.utils.string;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-31
 */
public class DateUtils {

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

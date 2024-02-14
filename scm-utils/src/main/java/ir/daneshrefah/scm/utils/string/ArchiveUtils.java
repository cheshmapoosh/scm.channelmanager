package ir.daneshrefah.scm.utils.string;

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
        return 10L;
    }

}
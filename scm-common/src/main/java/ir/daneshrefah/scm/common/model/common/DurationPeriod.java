package ir.daneshrefah.scm.common.model.common;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public enum DurationPeriod {

    DAY("1", "resource.uaa.duration-period.day"),
    WEEK("2", "resource.uaa.duration-period.week"),
    MONTH("3", "resource.uaa.duration-period.month"),
    YEAR("4", "resource.uaa.duration-period.year");

    DurationPeriod(String code, String title) {
        this.code = code;
        this.title = title;
    }

    private String code;
    private String title;

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public static DurationPeriod findByCode(String code) {
        DurationPeriod[] attrs = DurationPeriod.values();
        for (DurationPeriod attr : attrs) {
            if (attr.getCode().equals(code)) {
                return attr;
            }
        }
        return null;
    }

}

package ir.daneshrefah.scm.uaa.common.type;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public enum Gender {

    MALE("1", "resource.uaa.gender.male"),
    FEMALE("2", "resource.uaa.gender.female"),
    UNKNOWN("0", "resource.uaa.gender.unknown");

    Gender(String code, String title) {
        this.code = code;
        this.title = title;
    }

    private final String code;
    private final String title;

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public static Gender findByCode(String code) {
        Gender[] attrs = Gender.values();
        for (Gender attr : attrs) {
            if (attr.getCode().equals(code)) {
                return attr;
            }
        }
        return null;
    }

}

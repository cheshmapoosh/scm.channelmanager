package ir.daneshrefah.scm.uaa.common.type;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public enum Nationality {

    IRANIAN("I", "resource.uaa.nationality.iranian"),
    FOREIGN("F", "resource.uaa.nationality.foreign");

    Nationality(String code, String title) {
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

    public static Nationality findByCode(String code) {
        Nationality[] attrs = Nationality.values();
        for (Nationality attr : attrs) {
            if (attr.getCode().equals(code)) {
                return attr;
            }
        }
        return null;
    }

}

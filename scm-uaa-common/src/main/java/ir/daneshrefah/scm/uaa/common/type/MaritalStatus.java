package ir.daneshrefah.scm.uaa.common.type;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public enum MaritalStatus {

    MARRIED("M", "resource.uaa.marital-statue.married"),
    SINGLE("S", "resource.uaa.marital-statue.single");

    MaritalStatus(String code, String title) {
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

    public static MaritalStatus findByCode(String code) {
        MaritalStatus[] attrs = MaritalStatus.values();
        for (MaritalStatus attr : attrs) {
            if (attr.getCode().equals(code)) {
                return attr;
            }
        }
        return null;
    }

}

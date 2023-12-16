package ir.daneshrefah.scm.uaa.common.type;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public enum PersonType {

    INDIVIDUAL_CUSTOMER("1", "resource.uaa.person-type.individual"), //real
    EMPLOYEE("2", "resource.uaa.person-type.employee"),
    CORPORATE_CUSTOMER("3", "resource.uaa.person-type.corporate"), //legal;
    SYSTEM("4", "resource.uaa.person-type.system");

    PersonType(String code, String title) {
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

    public static PersonType findByCode(String code) {
        PersonType[] attrs = PersonType.values();
        for (PersonType attr : attrs) {
            if (attr.getCode().equals(code)) {
                return attr;
            }
        }
        return null;
    }

}

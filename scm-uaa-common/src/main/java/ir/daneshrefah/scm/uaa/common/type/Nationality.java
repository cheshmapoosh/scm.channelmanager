package ir.daneshrefah.scm.uaa.common.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@RequiredArgsConstructor
@Getter
public enum Nationality {

    IRANIAN("I"),
    FOREIGN("F");

    private final String code;
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

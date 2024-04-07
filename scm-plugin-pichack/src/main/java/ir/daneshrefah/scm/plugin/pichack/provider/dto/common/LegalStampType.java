package ir.daneshrefah.scm.plugin.pichack.provider.dto.common;

import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-06
 */
@RequiredArgsConstructor
public enum LegalStampType implements Serializable {

    /**
     * The IS Value.
     */
    IS(1),

    /**
     * The IS NOT Value.
     */
    IS_NOT(0);

    private final Integer code;

    public static LegalStampType findByCode(Integer code) {
        LegalStampType[] attrs = LegalStampType.values();
        for (LegalStampType attr : attrs) {
            if (attr.getCode().equals(code)) {
                return attr;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

}

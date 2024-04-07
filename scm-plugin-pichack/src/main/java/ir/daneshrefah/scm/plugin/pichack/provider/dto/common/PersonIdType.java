package ir.daneshrefah.scm.plugin.pichack.provider.dto.common;

import ir.daneshrefah.scm.utils.string.StringUtils;
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
public enum PersonIdType implements Serializable {

    /**
     * The REAL.
     */
    REAL(1),

    /**
     * The LEGAL.
     */
    LEGAL(2),

    /**
     * The REAL FOREIGNER.
     */
    FOREIGNER_REAL(3),

    /**
     * The LEGAL FOREIGNER.
     */
    FOREIGNER_LEGAL(4);

    private final Integer code;

    public static PersonIdType findByCode(String code) {
        if (StringUtils.isEmpty(code) || StringUtils.isNotNumeric(code))
            return null;

        return findByCode(Integer.valueOf(code));
    }

    public static PersonIdType findByCode(Integer code) {
        if (null == code)
            return null;

        PersonIdType[] attrs = PersonIdType.values();
        for (PersonIdType attr : attrs) {
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

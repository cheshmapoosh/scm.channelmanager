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
public enum ChequeType implements Serializable {

    /**
     * The REAL.
     */
    REGULAR(1),

    /**
     * The LEGAL.
     */
    BANK(2),

    /**
     * The REAL FOREIGNER.
     */
    PASSWORD_PROTECTED(3),

    /**
     * The omnibus, check moredi.
     */
    CASE_CHECK(4);

    private final Integer code;

    public static ChequeType findByCode(String code) {
        if (StringUtils.isEmpty(code))
            return null;
        ChequeType[] attrs = ChequeType.values();
        for (ChequeType attr : attrs) {
            if (String.valueOf(attr.getCode()).equals(code)) {
                return attr;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public static ChequeType findByCode(Integer code) {
        if (null == code)
            return null;
        ChequeType[] values = ChequeType.values();
        for (ChequeType value: values) {
            if (value.getCode() == code)
                return value;
        }
        return null;
    }
}

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
public enum ChequeMedia implements Serializable {

    /**
     * The PAPER.
     */
    PAPER(1),

    /**
     * The DIGITAL.
     */
    DIGITAL(2);

    private final Integer code;

    public static ChequeMedia findByCode(String code) {
        if (StringUtils.isEmpty(code))
            return null;
        ChequeMedia[] attrs = ChequeMedia.values();
        for (ChequeMedia attr : attrs) {
            if (String.valueOf(attr.getCode()).equals(code)) {
                return attr;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public static ChequeMedia findByCode(Integer code) {
        if (null == code)
            return null;
        ChequeMedia[] values = ChequeMedia.values();
        for (ChequeMedia value: values) {
            if (value.getCode() == code)
                return value;
        }
        return null;
    }
}

package ir.daneshrefah.scm.common.model.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-08
 */
@Getter
@RequiredArgsConstructor
public enum ServiceCompositionType {

    SAGA(1), FIRST_RESPONSE(2), FAILOVER(3), ROUND_ROBIN(4), AGGREGATE(5);

    private final Integer code;

    public static ServiceCompositionType findByCode(Integer code) {
        for (ServiceCompositionType enumValue : ServiceCompositionType.values()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        return null;
    }

}
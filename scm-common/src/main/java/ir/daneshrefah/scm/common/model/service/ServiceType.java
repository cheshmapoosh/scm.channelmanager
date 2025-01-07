package ir.daneshrefah.scm.common.model.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@Getter
@RequiredArgsConstructor
public enum ServiceType {

    REPORT(1), FINANCE(2), INQUIRY(3), PARENT(4), ENTITY_CREATE(5), ENTITY_UPDATE(6), ENTITY_DELETE(7);

    private final Integer code;

    public static ServiceType findByCode(Integer code) {
        for (ServiceType enumValue : ServiceType.values()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        return null;
    }
}

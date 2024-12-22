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
public enum ServiceImplementationType {

    CUSTOM_EXTERNAL (1),
    JAVA            (2),
    COMPOSITION     (3),
    BPMN            (4),
    PARENT          (5),
    REST_EXTERNAL   (6),
    PROXY           (7);

    private final Integer code;

    public static ServiceImplementationType findByCode(Integer code) {
        for (ServiceImplementationType enumValue : ServiceImplementationType.values()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        return null;
    }
}

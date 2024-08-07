package ir.daneshrefah.scm.common.model.service.parameter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-14
 */
@Getter
@RequiredArgsConstructor
public enum ParameterDatasourceProperty {

    /* provider based types */
    MESSAGE_VARIABLE(10), CONFIG_VARIABLE(11), RESOURCE_VARIABLE(12), CACHE_VARIABLE(13), STATIC(14),
    /* terminal based types */
    TERMINAL_CODE(20), PROVIDER_TERMINAL_CODE(21),
    /* date based types */
    DATE_YYYYMMDD(30), DATE_SHAMSI_YYYYMMDD(31),
    /* http based types */
    HTTP_STATUS_CODE(40),
    /* authentication based types */
    AUTHENTICATION_USERNAME(50), AUTHENTICATION_NICKNAME(51), AUTHENTICATION_EFFECTIVE_USERNAME(52), AUTHENTICATION_EFFECTIVE_NICKNAME(53),
    AUTHENTICATION_DELEGATOR_USERNAME(54), AUTHENTICATION_DELEGATOR_NICKNAME(55),
    /* request based type */
    CORRELATION_ID(60);

    private final Integer code;

    public static ParameterDatasourceProperty findByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        return Arrays.stream(values()).filter(param -> param.getCode().equals(code)).findFirst().orElse(null);
    }
}

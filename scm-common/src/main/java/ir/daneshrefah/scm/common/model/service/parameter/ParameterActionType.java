package ir.daneshrefah.scm.common.model.service.parameter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ParameterActionType {
    //REQUEST
    REQUEST_BODY(1),
    REQUEST_HEADER(2),
    REQUEST_QUERY_STRING(3),
    REQUEST_PATH_VARIABLE(4),
    //RESPONSE
    RESPONSE_BODY(5),
    RESPONSE_HEADER(6);

    private final Integer code;

    public static ParameterActionType findByCode(Integer code) {
        return Arrays.stream(values())
                .filter(parameterActionType -> parameterActionType.getCode().equals(code))
                .findFirst().orElse(null);
    }
}

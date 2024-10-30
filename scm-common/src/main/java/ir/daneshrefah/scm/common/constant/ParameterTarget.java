package ir.daneshrefah.scm.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ParameterTarget {
    PROVIDER, SERVICE, RESPONSE;

    public static ParameterTarget fromValue(String value) {
        return Arrays.stream(values())
                .filter(parameterTarget -> parameterTarget.name().equalsIgnoreCase(value))
                .findFirst().orElse(null);
    }


}

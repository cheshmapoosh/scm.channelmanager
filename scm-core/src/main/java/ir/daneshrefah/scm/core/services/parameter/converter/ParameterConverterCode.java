package ir.daneshrefah.scm.core.services.parameter.converter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParameterConverterCode {
    PERSON_TYPE("PersonTypeConverter");
    private final String code;

}

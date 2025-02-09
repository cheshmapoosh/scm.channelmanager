package ir.daneshrefah.scm.core.services.parameter.converter;

import ir.daneshrefah.scm.common.model.person.PersonType;
import org.springframework.stereotype.Component;

@Component
public class PersonTypeParameterConverter implements ParameterConverter<Integer, PersonType> {

    @Override
    public PersonType convert(Integer input) {
        if (null == input) {
            return null;
        }
        if (input.equals(0) || input.equals(-1)) {
            return PersonType.UNKNOWN;
        }
        return PersonType.findNabDetailCode(input);
    }

    @Override
    public Integer reverse(PersonType output) {
        return output.getDetailCode();
    }

    @Override
    public ParameterConverterCode getConverterCode() {
        return ParameterConverterCode.PERSON_TYPE;
    }
}

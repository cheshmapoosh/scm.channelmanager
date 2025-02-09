package ir.daneshrefah.scm.core.services.parameter.converter;

public interface ParameterConverter<I, O> {
    ParameterConverterCode getConverterCode();
    O convert(I input);
    I reverse(O output);
}


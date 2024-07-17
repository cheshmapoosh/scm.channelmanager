package ir.daneshrefah.scm.plugin.api.service;

import ir.daneshrefah.scm.plugin.api.model.service.external.parameter.Parameter;

import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-17
 */
public abstract class ParameterDataProvider {

    private static ParameterDataProvider INSTANCE;

    public abstract Optional<Object> extractParameterValue(Parameter parameter);

    protected static void setInstance(ParameterDataProvider provider) {
        if (null == INSTANCE) {
            INSTANCE = provider;
        } else  {
            throw new IllegalStateException("Instance already set");
        }
    }

    public static ParameterDataProvider getInstance() {
        return INSTANCE;
    }

}

package ir.daneshrefah.scm.plugin.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-27
 */
@Getter
@RequiredArgsConstructor
public abstract class ConverterDictionary<T> {

    private final T source;

    public abstract JsonNode convert(ParameterDefinition definition);

    @Data
    public static class ParameterDefinition {
        private String fromValue;
        private String type;
        private Integer length;
        private String converter;
        private boolean isMandatory;
    }

}

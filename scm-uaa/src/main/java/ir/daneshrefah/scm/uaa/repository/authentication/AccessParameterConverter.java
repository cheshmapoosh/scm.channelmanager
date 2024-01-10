package ir.daneshrefah.scm.uaa.repository.authentication;

import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-10
 */
@Converter
public class AccessParameterConverter implements AttributeConverter<Set<String>, String> {

    @Override
    public String convertToDatabaseColumn(Set<String> attribute) {
        return attribute == null ? null : String.join(",", attribute);
    }

    @Override
    public Set<String> convertToEntityAttribute(String dbData) {
        if (StringUtils.isEmpty(dbData)) {
            return null;
        }
        String[] values = dbData.split(";");
        return Arrays.stream(values)
                .filter(s -> !s.isEmpty())
                .filter(s -> !s.equalsIgnoreCase(","))
                .map(String::trim)
                .collect(Collectors.toSet());
    }
}

package ir.daneshrefah.scm.uaa.repository.authentication;

import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
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

    private static final int MOBILE_NUMBER_LENGTH = 11;

    @Override
    public String convertToDatabaseColumn(Set<String> attribute) {
        if (Objects.isNull(attribute)) {
            return null;
        }
        /* IF CONTENT HAS ONE VALUE AND THAT IS NOT MOBILE NUMBER */
        if (attribute.size() == 1 ) {
            String value = attribute.iterator().next();
            if (!(value.startsWith("0") && value.length() == MOBILE_NUMBER_LENGTH
                    || value.length() == MOBILE_NUMBER_LENGTH-1)) {
                return value;
            }
        }
        return attribute
                .stream()
                .map(p -> ";" + p + ";")
                .collect(Collectors.joining(","));
    }

    @Override
    public Set<String> convertToEntityAttribute(String dbData) {
        if (StringUtils.isEmpty(dbData)) {
            return null;
        }
        /* IF DATA IS MOBILE NUMBER */
        if (dbData.startsWith(";")) {
            String[] values = dbData.split(",");
            return Arrays.stream(values)
                    .filter(s -> !s.isEmpty())
                    .filter(s -> !s.equalsIgnoreCase(",") && !s.equalsIgnoreCase(";"))
                    .map(s -> s.replace(";", StringUtils.EMPTY).replace(",", StringUtils.EMPTY))
                    .map(String::trim)
                    .collect(Collectors.toSet());
        }
        HashSet<String> result = new HashSet<>();
        result.add(dbData);
        return result;
    }
}

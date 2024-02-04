package ir.daneshrefah.scm.common.data.converter;

import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-04
 */
@Converter(autoApply = true)
public class StringSetConverter implements AttributeConverter<Set<String>, String> {

    @Override
    public String convertToDatabaseColumn(Set<String> attribute) {
        if (null == attribute) {
            return null;
        }
        if (attribute.isEmpty()) {
            return StringUtils.EMPTY;
        }
        return String.join(",", attribute);
    }

    @Override
    public Set<String> convertToEntityAttribute(String dbData) {
        if (StringUtils.isEmpty(dbData)) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(dbData.split(",")));
    }
}

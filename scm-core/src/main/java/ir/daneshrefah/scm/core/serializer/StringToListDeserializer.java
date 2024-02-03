package ir.daneshrefah.scm.core.serializer;

import com.fasterxml.jackson.databind.util.StdConverter;
import ir.daneshrefah.scm.utils.string.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-03
 */
public class StringToListDeserializer extends StdConverter<String, List<String>> {

    @Override
    public List<String> convert(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return Arrays.asList(value.split(","));
    }

}

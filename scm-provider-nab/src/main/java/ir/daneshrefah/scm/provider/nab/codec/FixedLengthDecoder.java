package ir.daneshrefah.scm.provider.nab.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.provider.nab.domain.NabFieldSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FixedLengthDecoder {
    private final ObjectMapper objectMapper;
    private final NabValueConverterRegistry converterRegistry;

    public ObjectNode decode(String fragment, List<NabFieldSpec> fields) {
        ObjectNode result = objectMapper.createObjectNode();
        int offset = 0;
        for (NabFieldSpec field : fields) {
            String rawValue = slice(fragment, offset, field);
            Object value = converterRegistry.decode(rawValue, field);
            put(result, field.name(), value);
            offset += field.length();
        }
        return result;
    }

    private String slice(String fragment, int offset, NabFieldSpec field) {
        String safeFragment = fragment == null ? "" : fragment;
        if (safeFragment.length() < offset + field.length()) {
            if (field.required()) {
                throw new IllegalArgumentException("NAB response field " + field.name() + " is shorter than " + field.length());
            }
            if (safeFragment.length() <= offset) {
                return "";
            }
            return safeFragment.substring(offset);
        }
        return safeFragment.substring(offset, offset + field.length());
    }

    private void put(ObjectNode node, String name, Object value) {
        if (value == null) {
            node.putNull(name);
        } else if (value instanceof Integer integer) {
            node.put(name, integer);
        } else if (value instanceof Long longValue) {
            node.put(name, longValue);
        } else if (value instanceof BigDecimal decimal) {
            node.put(name, decimal);
        } else if (value instanceof Boolean booleanValue) {
            node.put(name, booleanValue);
        } else {
            node.put(name, String.valueOf(value));
        }
    }
}

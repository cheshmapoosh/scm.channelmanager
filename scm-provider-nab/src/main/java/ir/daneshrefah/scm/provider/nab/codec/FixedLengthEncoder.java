package ir.daneshrefah.scm.provider.nab.codec;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.provider.nab.domain.NabFieldSpec;
import ir.daneshrefah.scm.provider.nab.domain.NabOverflowPolicy;
import ir.daneshrefah.scm.provider.nab.domain.NabPadding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FixedLengthEncoder {
    private final NabValueConverterRegistry converterRegistry;

    public String encode(JsonNode data, List<NabFieldSpec> fields) {
        StringBuilder builder = new StringBuilder();
        for (NabFieldSpec field : fields) {
            String value = value(data, field);
            String converted = converterRegistry.encode(value, field);
            String adjusted = adjust(converted, field);
            if (log.isTraceEnabled()) {
                log.trace("NAB encode field name={} path={} length={} value={} adjusted={}",
                        field.name(), field.path(), field.length(), value, adjusted);
            }
            builder.append(adjusted);
        }
        return builder.toString();
    }

    public String adjust(String value, NabFieldSpec field) {
        String adjusted = value == null ? "" : value;
        if (adjusted.length() > field.length()) {
            if (field.overflow() == NabOverflowPolicy.TRUNCATE) {
                adjusted = adjusted.substring(0, field.length());
            } else {
                throw new IllegalArgumentException("NAB field " + field.name()
                        + " length is " + adjusted.length() + " but max is " + field.length());
            }
        }
        if (adjusted.length() == field.length() || field.padding() == NabPadding.NONE) {
            return adjusted;
        }
        return switch (field.padding()) {
            case RIGHT_SPACE -> adjusted + " ".repeat(field.length() - adjusted.length());
            case LEFT_SPACE -> " ".repeat(field.length() - adjusted.length()) + adjusted;
            case LEFT_ZERO -> "0".repeat(field.length() - adjusted.length()) + adjusted;
            case NONE -> adjusted;
        };
    }

    private String value(JsonNode data, NabFieldSpec field) {
        String value = JsonNodeSupport.textAt(data, field.path());
        if (value == null || value.isEmpty()) {
            if (field.required()) {
                throw new IllegalArgumentException("NAB field " + field.name() + " is required");
            }
            return "";
        }
        return value;
    }
}

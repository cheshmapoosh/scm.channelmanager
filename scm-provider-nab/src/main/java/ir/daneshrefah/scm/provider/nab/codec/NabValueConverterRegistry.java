package ir.daneshrefah.scm.provider.nab.codec;

import ir.daneshrefah.scm.provider.nab.domain.NabFieldSpec;
import ir.daneshrefah.scm.provider.nab.domain.NabFieldType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;

@Component
public class NabValueConverterRegistry {

    public String encode(String value, NabFieldSpec fieldSpec) {
        String converted = value == null ? "" : value;
        converted = applyNamedConverter(converted, fieldSpec.converter(), true);
        return switch (fieldSpec.type()) {
            case STRING, RAW, DATE, DATETIME -> converted;
            case NUMBER -> requireNumber(converted, fieldSpec.name());
            case DECIMAL -> requireDecimal(converted, fieldSpec.name());
            case BOOLEAN -> toBooleanWireValue(converted);
        };
    }

    public Object decode(String value, NabFieldSpec fieldSpec) {
        String converted = value == null ? "" : value;
        if (fieldSpec.trim()) {
            converted = converted.trim();
        }
        converted = applyNamedConverter(converted, fieldSpec.converter(), false);
        if (converted.isBlank() && !fieldSpec.required()) {
            return "";
        }
        return switch (fieldSpec.type()) {
            case STRING, RAW, DATE, DATETIME -> converted;
            case NUMBER -> Long.parseLong(requireNumber(converted, fieldSpec.name()));
            case DECIMAL -> new BigDecimal(requireDecimal(converted, fieldSpec.name()));
            case BOOLEAN -> fromBooleanWireValue(converted);
        };
    }

    private String applyNamedConverter(String value, String converter, boolean encode) {
        if (StringUtils.isBlank(converter) || "NONE".equalsIgnoreCase(converter)) {
            return value;
        }
        String key = converter.trim().toUpperCase(Locale.ROOT);
        return switch (key) {
            case "TRIM" -> value.trim();
            case "UPPERCASE" -> value.toUpperCase(Locale.ROOT);
            case "LOWERCASE" -> value.toLowerCase(Locale.ROOT);
            case "BOOLEAN_1_0" -> encode ? toBooleanWireValue(value) : value;
            case "PERSIAN_DIGITS_TO_ENGLISH" -> persianDigitsToEnglish(value);
            case "ENGLISH_DIGITS_TO_PERSIAN" -> englishDigitsToPersian(value);
            case "ARABIC_TO_PERSIAN_CHARS" -> arabicToPersianChars(value);
            case "PERSIAN_TO_ARABIC_CHARS" -> persianToArabicChars(value);
            default -> throw new IllegalArgumentException("Unsupported NAB field converter: " + converter);
        };
    }

    private String requireNumber(String value, String fieldName) {
        if (value.isBlank()) {
            return value;
        }
        if (!value.matches("-?\\d+")) {
            throw new IllegalArgumentException("NAB field " + fieldName + " must be a number");
        }
        return value;
    }

    private String requireDecimal(String value, String fieldName) {
        if (value.isBlank()) {
            return value;
        }
        try {
            return new BigDecimal(value).toPlainString();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("NAB field " + fieldName + " must be a decimal", e);
        }
    }

    private String toBooleanWireValue(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return ("true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized) || "y".equals(normalized))
                ? "1"
                : "0";
    }

    private boolean fromBooleanWireValue(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return "true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized) || "y".equals(normalized);
    }

    private String persianDigitsToEnglish(String value) {
        return value
                .replace('۰', '0')
                .replace('۱', '1')
                .replace('۲', '2')
                .replace('۳', '3')
                .replace('۴', '4')
                .replace('۵', '5')
                .replace('۶', '6')
                .replace('۷', '7')
                .replace('۸', '8')
                .replace('۹', '9');
    }

    private String englishDigitsToPersian(String value) {
        return value
                .replace('0', '۰')
                .replace('1', '۱')
                .replace('2', '۲')
                .replace('3', '۳')
                .replace('4', '۴')
                .replace('5', '۵')
                .replace('6', '۶')
                .replace('7', '۷')
                .replace('8', '۸')
                .replace('9', '۹');
    }

    private String arabicToPersianChars(String value) {
        return value.replace('ي', 'ی').replace('ك', 'ک');
    }

    private String persianToArabicChars(String value) {
        return value.replace('ی', 'ي').replace('ک', 'ك');
    }
}

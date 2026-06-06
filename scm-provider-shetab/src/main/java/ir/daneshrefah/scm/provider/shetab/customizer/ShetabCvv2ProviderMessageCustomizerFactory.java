package ir.daneshrefah.scm.provider.shetab.customizer;

import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryContext;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

@Component
public class ShetabCvv2ProviderMessageCustomizerFactory
        implements ProviderMessageCustomizerFactory<ShetabCvv2ProviderMessageCustomizerFactory.Config> {
    @Override
    public String type() {
        return "shetab-cvv2";
    }

    @Override
    public Class<Config> configType() {
        return Config.class;
    }

    @Override
    public int defaultOrder() {
        return 210;
    }

    @Override
    public ProviderMessageCustomizer create(ProviderMessageCustomizerFactoryContext context, Config config) {
        HpsShetabOutletProviderMessageCustomizerFactory.validateContext(context, type());
        Config safe = config == null ? new Config() : config;
        safe.validate(context.providerCode());
        return new Cvv2Customizer(safe.field, safe.tag, safe.source, safe.lengthDigits, safe.minLength, safe.maxLength, defaultOrder());
    }

    @Getter
    @Setter
    public static class Config {
        private Integer field = 48;
        private String tag = "P92";
        private String source = "security.cvv2";
        private Integer lengthDigits = 3;
        private Integer minLength = 3;
        private Integer maxLength = 4;

        void validate(String providerCode) {
            if (field == null || field < 1 || StringUtils.isBlank(tag)) {
                throw new IllegalArgumentException("shetab-cvv2.field and tag are required for provider " + providerCode);
            }
        }
    }

    record Cvv2Customizer(int field, String tag, String source, int lengthDigits, int minLength, int maxLength, int order)
            implements ProviderMessageCustomizer {
        @Override
        public void beforeSend(ProviderExchange exchange) {
            ISOMsg msg = ShetabCustomizerSupport.isoMessage(exchange);
            String original = StringUtils.defaultString(ShetabCustomizerSupport.safeField(msg, field));
            TagRemovalResult cleaned = removeTagSegment(original, tag, lengthDigits);
            String cvv2 = ShetabCustomizerSupport.sourceValue(exchange, source);
            if (StringUtils.isBlank(cvv2)) {
                ShetabCustomizerSupport.setIsoField(exchange, msg, field, cleaned.value());
                if (ShetabCustomizerSupport.booleanSource(exchange, "cvv2Required")) {
                    throw new IllegalArgumentException("Shetab CVV2 value is required for provider=" + exchange.context().providerCode());
                }
                return;
            }
            if (!cvv2.matches("\\d+")) {
                throw new IllegalArgumentException("Shetab CVV2 must be numeric");
            }
            if (cvv2.length() < Math.max(1, minLength) || cvv2.length() > Math.max(minLength, maxLength)) {
                throw new IllegalArgumentException("Shetab CVV2 length is invalid for provider=" + exchange.context().providerCode());
            }
            ShetabCustomizerSupport.setIsoField(exchange, msg, field, cleaned.value() + buildTagSegment(tag, cvv2, lengthDigits));
        }

        private TagRemovalResult removeTagSegment(String source, String targetTag, int lengthDigits) {
            if (StringUtils.isBlank(source)) {
                return new TagRemovalResult("", false);
            }
            StringBuilder rebuilt = new StringBuilder(source.length());
            boolean removed = false;
            int index = 0;
            int safeLengthDigits = Math.max(1, lengthDigits);
            int headerLength = 3 + safeLengthDigits;
            while (index + headerLength <= source.length()) {
                String currentTag = source.substring(index, index + 3);
                String lengthText = source.substring(index + 3, index + headerLength);
                if (!StringUtils.isNumeric(lengthText)) {
                    break;
                }
                int valueLength = Integer.parseInt(lengthText);
                int valueStart = index + headerLength;
                int valueEnd = valueStart + valueLength;
                if (valueEnd > source.length()) {
                    break;
                }
                if (targetTag.equals(currentTag)) {
                    removed = true;
                } else {
                    rebuilt.append(currentTag).append(lengthText).append(source, valueStart, valueEnd);
                }
                index = valueEnd;
            }
            if (index < source.length()) {
                rebuilt.append(source.substring(index));
            }
            return new TagRemovalResult(rebuilt.toString(), removed);
        }

        private String buildTagSegment(String tag, String value, int lengthDigits) {
            int safeLengthDigits = Math.max(1, lengthDigits);
            int maxValueLength = (int) Math.pow(10, safeLengthDigits) - 1;
            if (value.length() > maxValueLength) {
                throw new IllegalArgumentException("Shetab CVV2 value length exceeds configured lengthDigits");
            }
            return tag + String.format("%0" + safeLengthDigits + "d", value.length()) + value;
        }

        private record TagRemovalResult(String value, boolean removed) {
        }
    }
}

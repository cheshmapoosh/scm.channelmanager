package ir.daneshrefah.scm.provider.shetab.security;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabPackagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShetabMessageSecurityProcessor {
    private final ShetabPackagerFactory packagerFactory;
    private final ShetabSecurityCrypto crypto = new ShetabSecurityCrypto();

    public void protectRequest(ShetabResolvedConfig config, Map<String, Object> requestBody, ISOMsg request) {
        request.setPackager(packagerFactory.create(config));
        applyCardSecurity(config, requestBody, request);
        applyMac(config, requestBody, request);
    }

    public void verifyResponse(ShetabResolvedConfig config, ISOMsg response) {
        ShetabResolvedConfig.Mac mac = security(config).mac();
        if (mac == null || !mac.verifyResponse()) {
            return;
        }
        String receivedMac = safeField(response, mac.field());
        if (StringUtils.isBlank(receivedMac)) {
            throw new IllegalStateException("Shetab response MAC field is empty for provider=" + config.provider());
        }
        try {
            response.setPackager(packagerFactory.create(config));
            String calculatedMac = calculateMac(response, mac);
            response.set(mac.field(), receivedMac);
            if (!receivedMac.equalsIgnoreCase(calculatedMac)) {
                throw new IllegalStateException("Shetab response MAC verification failed for provider=" + config.provider());
            }
        } catch (ISOException e) {
            throw new IllegalStateException("Could not verify Shetab response MAC for provider=" + config.provider(), e);
        }
    }

    private void applyCardSecurity(ShetabResolvedConfig config, Map<String, Object> requestBody, ISOMsg request) {
        applyExpiryDate(config, requestBody, request);
        applyCvv2Tag(config, requestBody, request);
        applyPin(config, requestBody, request);
    }

    private void applyPin(ShetabResolvedConfig config, Map<String, Object> requestBody, ISOMsg request) {
        ShetabResolvedConfig.Pin pin = security(config).pin();
        String rawPin = rawPin(requestBody);
        boolean pinRequired = booleanSecurity(requestBody, "pinRequired");

        if (pin != null && request.hasField(pin.field())) {
            request.unset(pin.field());
            log.warn("Ignoring caller supplied Shetab PIN block field. provider={} field={}", config.provider(), pin.field());
        }

        if (!pinRequired) {
            return;
        }
        if (pin == null || StringUtils.isBlank(pin.key())) {
            throw new IllegalArgumentException("Shetab PIN key is required for provider=" + config.provider());
        }
        if (StringUtils.isBlank(rawPin)) {
            throw new IllegalArgumentException("Raw PIN is required for provider=" + config.provider());
        }

        String pan = firstNonBlank(safeField(request, pin.panField()), stringSecurity(requestBody, "pan"));
        String pinBlock = crypto.generatePinBlock(rawPin, pan, pin.key());
        request.set(pin.field(), pinBlock);
    }


    private void applyExpiryDate(ShetabResolvedConfig config, Map<String, Object> requestBody, ISOMsg request) {
        ShetabResolvedConfig.Expiry expiry = security(config).expiry();
        int expiryField = expiry != null ? expiry.field() : 14;
        boolean expiryRequired = booleanSecurity(requestBody, "expiryRequired")
                || booleanSecurity(requestBody, "expRequired")
                || booleanSecurity(requestBody, "expirationRequired");
        if (request.hasField(expiryField)) {
            request.unset(expiryField);
            log.warn("Ignoring caller supplied Shetab expiry field. provider={} field={}", config.provider(), expiryField);
        }
        if (!expiryRequired) {
            return;
        }
        String expiryDate = expiryDate(requestBody);
        if (StringUtils.isBlank(expiryDate)) {
            throw new IllegalArgumentException("Shetab security expiryDate is required for provider=" + config.provider());
        }
        if (!expiryDate.matches("\\d{4}")) {
            throw new IllegalArgumentException("Shetab security expiryDate must be 4 digits (YYMM)");
        }
        request.set(expiryField, expiryDate);
    }

    private void applyCvv2Tag(ShetabResolvedConfig config, Map<String, Object> requestBody, ISOMsg request) {
        ShetabResolvedConfig.Cvv2 cvv2Config = security(config).cvv2();
        int cvv2Field = cvv2Config != null ? cvv2Config.field() : 48;
        String cvv2Tag = cvv2Config != null ? cvv2Config.tag() : "P92";
        int lengthDigits = cvv2Config != null ? cvv2Config.lengthDigits() : 3;
        int minLength = cvv2Config != null ? cvv2Config.minLength() : 3;
        int maxLength = cvv2Config != null ? cvv2Config.maxLength() : 4;
        boolean cvv2Required = booleanSecurity(requestBody, "cvv2Required") || booleanSecurity(requestBody, "cvvRequired");

        String originalFieldValue = StringUtils.defaultString(safeField(request, cvv2Field));
        TagRemovalResult cleanedField = removeTagSegment(originalFieldValue, cvv2Tag, lengthDigits);
        if (cleanedField.removed()) {
            log.warn("Ignoring caller supplied Shetab CVV2 tag in field {} ({}) for provider={}",
                    cvv2Field, cvv2Tag, config.provider());
        }

        if (!cvv2Required) {
            setField(request, cvv2Field, cleanedField.value());
            return;
        }

        String cvv2 = cvv2(requestBody);
        if (StringUtils.isBlank(cvv2)) {
            throw new IllegalArgumentException("Shetab security cvv2 is required for provider=" + config.provider());
        }
        if (!cvv2.matches("\\d+")) {
            throw new IllegalArgumentException("Shetab security cvv2 must be numeric");
        }
        if (cvv2.length() < Math.max(1, minLength) || cvv2.length() > Math.max(minLength, maxLength)) {
            throw new IllegalArgumentException("Shetab security cvv2 length is invalid for provider=" + config.provider());
        }
        String rebuiltFieldValue = cleanedField.value() + buildTagSegment(cvv2Tag, cvv2, lengthDigits);
        setField(request, cvv2Field, rebuiltFieldValue);
    }

    private void applyMac(ShetabResolvedConfig config, Map<String, Object> requestBody, ISOMsg request) {
        ShetabResolvedConfig.Mac mac = security(config).mac();
        boolean macRequired = booleanSecurity(requestBody, "macRequired");

        if (mac != null && request.hasField(mac.field())) {
            request.unset(mac.field());
            log.warn("Ignoring caller supplied Shetab MAC field. provider={} field={}", config.provider(), mac.field());
        }

        if (!macRequired) {
            return;
        }
        if (mac == null || StringUtils.isBlank(mac.key())) {
            throw new IllegalArgumentException("Shetab MAC key is required for provider=" + config.provider());
        }

        try {
            request.set(mac.field(), calculateMac(request, mac));
        } catch (ISOException e) {
            throw new IllegalStateException("Could not generate Shetab request MAC for provider=" + config.provider(), e);
        }
    }

    private String calculateMac(ISOMsg message, ShetabResolvedConfig.Mac mac) throws ISOException {
        String placeholder = StringUtils.defaultIfBlank(mac.placeholder(), "AAAAAAAAAAAAAAAA");
        message.set(mac.field(), placeholder);
        byte[] packed = message.pack();
        int macLength = mac.packedLengthBytes() > 0 ? mac.packedLengthBytes() : placeholder.length();
        if (packed.length <= macLength) {
            throw new IllegalStateException("Packed Shetab message is shorter than configured MAC length");
        }
        byte[] macInput = Arrays.copyOf(packed, packed.length - macLength);
        return crypto.generateIso9797Mac(macInput, mac.key());
    }

    private ShetabResolvedConfig.Security security(ShetabResolvedConfig config) {
        if (config.security() != null) {
            return config.security();
        }
        return new ShetabResolvedConfig.Security(
                new ShetabResolvedConfig.Pin(false, null, 52, 2),
                new ShetabResolvedConfig.Mac(false, null, 128, false, "AAAAAAAAAAAAAAAA", 16),
                new ShetabResolvedConfig.Expiry(false, 14),
                new ShetabResolvedConfig.Cvv2(false, 48, "P92", 3, 3, 4)
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> securityMap(Map<String, Object> requestBody) {
        Object security = requestBody == null ? null : requestBody.get("security");
        if (security instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private String rawPin(Map<String, Object> requestBody) {
        return firstNonBlank(stringSecurity(requestBody, "pin"), stringValue(requestBody == null ? null : requestBody.get("pin")));
    }

    private String expiryDate(Map<String, Object> requestBody) {
        return firstNonBlank(
                stringSecurity(requestBody, "expiryDate"),
                firstNonBlank(stringSecurity(requestBody, "expirationDate"), stringValue(requestBody == null ? null : requestBody.get("expiryDate")))
        );
    }

    private String cvv2(Map<String, Object> requestBody) {
        return firstNonBlank(
                stringSecurity(requestBody, "cvv2"),
                firstNonBlank(stringSecurity(requestBody, "cvv"), stringValue(requestBody == null ? null : requestBody.get("cvv2")))
        );
    }

    private void setField(ISOMsg request, int field, String value) {
        if (StringUtils.isBlank(value)) {
            request.unset(field);
            return;
        }
        request.set(field, value);
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
            String tag = source.substring(index, index + 3);
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
            if (targetTag.equals(tag)) {
                removed = true;
            } else {
                rebuilt.append(tag).append(lengthText).append(source, valueStart, valueEnd);
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
        String format = "%0" + safeLengthDigits + "d";
        return tag + String.format(format, value.length()) + value;
    }

    private boolean booleanSecurity(Map<String, Object> requestBody, String key) {
        Object value = securityMap(requestBody).get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }

    private String stringSecurity(Map<String, Object> requestBody, String key) {
        return stringValue(securityMap(requestBody).get(key));
    }

    private String stringValue(Object value) {
        return value == null ? null : StringUtils.trimToNull(String.valueOf(value));
    }

    private String firstNonBlank(String first, String second) {
        return StringUtils.isNotBlank(first) ? first : second;
    }

    private String safeField(ISOMsg msg, int field) {
        try {
            return msg != null ? msg.getString(field) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private record TagRemovalResult(String value, boolean removed) {
    }
}

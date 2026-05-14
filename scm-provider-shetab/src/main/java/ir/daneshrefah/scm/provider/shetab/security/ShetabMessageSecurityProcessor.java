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
        applyPin(config, requestBody, request);
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

    private void applyPin(ShetabResolvedConfig config, Map<String, Object> requestBody, ISOMsg request) {
        ShetabResolvedConfig.Pin pin = security(config).pin();
        String rawPin = rawPin(requestBody);
        boolean pinRequired = pin != null && pin.enabled() || booleanSecurity(requestBody, "pinRequired");

        if (pin != null && request.hasField(pin.field())) {
            request.unset(pin.field());
            log.warn("Ignoring caller supplied Shetab PIN block field. provider={} field={}", config.provider(), pin.field());
        }

        if (!pinRequired && StringUtils.isBlank(rawPin)) {
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

    private void applyMac(ShetabResolvedConfig config, Map<String, Object> requestBody, ISOMsg request) {
        ShetabResolvedConfig.Mac mac = security(config).mac();
        boolean macRequired = mac != null && mac.enabled() || booleanSecurity(requestBody, "macRequired");

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
                new ShetabResolvedConfig.Mac(false, null, 128, false, "AAAAAAAAAAAAAAAA", 16)
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
}

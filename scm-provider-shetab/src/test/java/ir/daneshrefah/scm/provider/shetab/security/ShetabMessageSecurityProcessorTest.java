package ir.daneshrefah.scm.provider.shetab.security;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabIsoMapConverter;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabPackagerFactory;
import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShetabMessageSecurityProcessorTest {
    private final ShetabIsoMapConverter converter = new ShetabIsoMapConverter();
    private final ShetabMessageSecurityProcessor processor = new ShetabMessageSecurityProcessor(
            new ShetabPackagerFactory(new DefaultResourceLoader())
    );

    @Test
    void generatesPinBlockAndMacInsideProvider() throws Exception {
        Map<String, Object> requestBody = requestBody();
        @SuppressWarnings("unchecked")
        Map<String, Object> fields = (Map<String, Object>) requestBody.get("fields");
        fields.put("52", "*****");
        fields.put("128", "0000000000000000");
        requestBody.put("security", Map.of(
                "pin", "1234",
                "pinRequired", true,
                "macRequired", true
        ));

        ISOMsg request = converter.toIsoMsg(requestBody);
        processor.protectRequest(config(true, true), requestBody, request);

        assertEquals("1100", request.getMTI());
        assertEquals("5894631159226349", request.getString(2));
        assertTrue(request.hasField(52));
        assertTrue(request.hasField(128));
        assertNotEquals("*****", request.getString(52));
        assertNotEquals("0000000000000000", request.getString(128));
        assertEquals(16, request.getString(52).length());
        assertEquals(16, request.getString(128).length());
    }

    @Test
    void removesCallerSuppliedSecurityFieldsWhenGenerationIsDisabled() {
        Map<String, Object> requestBody = requestBody();
        @SuppressWarnings("unchecked")
        Map<String, Object> fields = (Map<String, Object>) requestBody.get("fields");
        fields.put("52", "caller-pin-block");
        fields.put("128", "caller-mac");

        ISOMsg request = converter.toIsoMsg(requestBody);
        processor.protectRequest(config(false, false), requestBody, request);

        assertFalse(request.hasField(52));
        assertFalse(request.hasField(128));
    }

    private Map<String, Object> requestBody() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("2", "5894631159226349");
        fields.put("3", "330000");
        fields.put("4", "000000000000");
        fields.put("6", "000000000000");
        fields.put("7", "0513140402");
        fields.put("11", "261655");
        fields.put("12", "260513140402");
        fields.put("14", "0506");
        fields.put("22", "61051061314C");
        fields.put("24", "113");
        fields.put("26", "6012");
        fields.put("32", "589463");
        fields.put("33", "589463");
        fields.put("37", "691199261655");
        fields.put("41", "67777777");
        fields.put("42", "   777777777600");
        fields.put("43", "Refah Bank            Tehran       THRIR010010157171371502184852851");
        fields.put("48", "P92003086DST0165894631240207217");
        fields.put("49", "364");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mti", "1100");
        body.put("fields", fields);
        return body;
    }

    private ShetabResolvedConfig config(boolean pinEnabled, boolean macEnabled) {
        return new ShetabResolvedConfig(
                "hps",
                List.of("127.0.0.1:9000"),
                "Shetab7AsciiXAPackager",
                null,
                3000,
                1000,
                6000,
                1000,
                1000,
                3,
                1000,
                new ShetabResolvedConfig.RateLimit(false, "unused", "provider"),
                new ShetabResolvedConfig.EndpointLease(false, 30_000L),
                new ShetabResolvedConfig.Security(
                        new ShetabResolvedConfig.Pin(pinEnabled, "0123456789ABCDEF", 52, 2),
                        new ShetabResolvedConfig.Mac(macEnabled, "0123456789ABCDEF", 128, false, "AAAAAAAAAAAAAAAA", 16)
                )
        );
    }

    @Test
    void rawPinComesFromOperationBodyAndProviderGeneratesPinBlockWithConfiguredKey() throws Exception {
        Map<String, Object> requestBody = requestBody();
        requestBody.put("security", Map.of("pin", "1234"));

        ISOMsg request = converter.toIsoMsg(requestBody);
        processor.protectRequest(config(false, false), requestBody, request);

        assertTrue(request.hasField(52));
        assertEquals(16, request.getString(52).length());
    }
}

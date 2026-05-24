package ir.daneshrefah.scm.provider.shetab.scenario;

import ir.daneshrefah.scm.cache.client.utility.resourcelease.LocalResourceLeaseUtility;
import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabIsoMapConverter;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabPackagerFactory;
import ir.daneshrefah.scm.provider.shetab.lease.CacheClientShetabEndpointLeaseManager;
import ir.daneshrefah.scm.provider.shetab.metrics.ShetabProviderMetrics;
import ir.daneshrefah.scm.provider.shetab.security.ShetabMessageSecurityProcessor;
import ir.daneshrefah.scm.provider.shetab.tcp.ShetabIsoChannelClient;
import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integration")
class ShetabHpsCardInquiryIntegrationTest {
    private static final Path CARD_INQUIRY_SAMPLE_PATH;

    static {
        try {
            CARD_INQUIRY_SAMPLE_PATH = new ClassPathResource(
                    "ir/daneshrefah/scm/provider/shetab/scenario/1100.xml")
                    .getFile().toPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final DateTimeFormatter TRANSMISSION_DATE_TIME = DateTimeFormatter.ofPattern("MMddHHmmss");
    private static final DateTimeFormatter LOCAL_TRANSACTION_DATE_TIME = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private final ShetabIsoMapConverter converter = new ShetabIsoMapConverter();
    private final ShetabPackagerFactory packagerFactory = new ShetabPackagerFactory(new DefaultResourceLoader());
    private final ShetabMessageSecurityProcessor securityProcessor = new ShetabMessageSecurityProcessor(packagerFactory);

    @Test
    void sendsCardInquiryToHpsAndReceivesNetworkResponse() throws Exception {
        Assumptions.assumeTrue(Boolean.parseBoolean(env("SCM_SHETAB_HPS_INTEGRATION", "false")),
                "Set SCM_SHETAB_HPS_INTEGRATION=true to run the real HPS integration test");
        Assumptions.assumeTrue(Files.exists(CARD_INQUIRY_SAMPLE_PATH),
                "Sample file does not exist: " + CARD_INQUIRY_SAMPLE_PATH);

        List<String> endpoints = endpoints();
        String exp = env("SCM_SHETAB_HPS_EXP", "");
        String cvv2 = env("SCM_SHETAB_HPS_CVV2", "");
        String pin = env("SCM_SHETAB_HPS_PIN", "");
        ShetabResolvedConfig config = config(
                endpoints,
                env("SCM_SHETAB_HPS_PIN_KEY", ""),
                env("SCM_SHETAB_HPS_MAC_KEY", "")
        );

        Map<String, Object> requestBody = readRequest1100(CARD_INQUIRY_SAMPLE_PATH);
        prepareDynamicFields(requestBody);
        Map<String, Serializable> security = Map.of(
                "expiryDate", exp,
                "cvv2", cvv2,
                "pin", pin,
                "expiryRequired", false,
                "cvv2Required",  false,
                "pinRequired", false,
                "macRequired", false
        );
        requestBody.put("security", security);

        ISOMsg request = converter.toIsoMsg(requestBody);
        securityProcessor.protectRequest(config, requestBody, request);

        ShetabIsoChannelClient client = new ShetabIsoChannelClient(
                config,
                packagerFactory,
                new CacheClientShetabEndpointLeaseManager(new LocalResourceLeaseUtility()),
                new ShetabProviderMetrics()
        );

        client.start();
        try {
            ISOMsg response = client.request(request, config.responseTimeoutMs());
            assertEquals("1110", response.getMTI());
            assertEquals(request.getString(11), response.getString(11));
            assertEquals(request.getString(37), response.getString(37));
            assertNotNull(response.getString(39));
        } finally {
            client.stop();
        }
    }

    private List<String> endpoints() {
        String endpointText = env("SCM_SHETAB_HPS_ENDPOINTS", env("SCM_SHETAB_HPS_ENDPOINT", "10.15.1.61:32001"));
        Assumptions.assumeTrue(!endpointText.isBlank(), "Set SCM_SHETAB_HPS_ENDPOINTS=ip:port[,ip:port...]");
        return Arrays.stream(endpointText.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private ShetabResolvedConfig config(List<String> endpoints, String pinKey, String macKey) {
        int responseTimeoutMs = Integer.parseInt(env("SCM_SHETAB_HPS_RESPONSE_TIMEOUT_MS", "10000"));
        int socketTimeoutMs = Integer.parseInt(env("SCM_SHETAB_HPS_SOCKET_TIMEOUT_MS", "1000"));
        return new ShetabResolvedConfig(
                "hps",
                endpoints,
                env("SCM_SHETAB_HPS_PACKAGER", "Shetab7AsciiXAPackager"),
                null,
                3000,
                socketTimeoutMs,
                responseTimeoutMs,
                1000,
                1000,
                3,
                1000,
                new ShetabResolvedConfig.RateLimit(false, "unused", "provider"),
                new ShetabResolvedConfig.EndpointLease(true, 30_000L),
                new ShetabResolvedConfig.Security(
                        new ShetabResolvedConfig.Pin(true, pinKey, 52, 2),
                        new ShetabResolvedConfig.Mac(true, macKey, 128, false, "AAAAAAAAAAAAAAAA", 16)
                )
        );
    }

    @SuppressWarnings("unchecked")
    private void prepareDynamicFields(Map<String, Object> requestBody) {
        Map<String, Object> fields = (Map<String, Object>) requestBody.get("fields");
        LocalDateTime now = LocalDateTime.now();
        String stan = String.format("%06d", System.currentTimeMillis() % 1_000_000);
        String rrn = String.format("%012d", System.currentTimeMillis() % 1_000_000_000_000L);

        fields.put("7", now.format(TRANSMISSION_DATE_TIME));
        fields.put("11", stan);
        fields.put("12", now.format(LOCAL_TRANSACTION_DATE_TIME));
        fields.put("37", rrn);
        fields.remove("52");
        fields.remove("128");
    }

    private Map<String, Object> readRequest1100(Path xmlPath) throws Exception {
        String xmlContent = Files.readString(xmlPath, StandardCharsets.UTF_8);
        String wrappedXml = "<root>" + xmlContent + "</root>";
        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(wrappedXml.getBytes(StandardCharsets.UTF_8)));

        NodeList isoNodes = document.getElementsByTagName("isomsg");
        for (int index = 0; index < isoNodes.getLength(); index++) {
            Node node = isoNodes.item(index);
            if (!(node instanceof Element isoElement)) {
                continue;
            }
            Map<String, Object> body = toBody(isoElement);
            if ("1100".equals(body.get("mti"))) {
                return body;
            }
        }
        throw new IllegalStateException("Could not find 1100 request in " + xmlPath);
    }

    private Map<String, Object> toBody(Element isoElement) {
        Map<String, Object> fields = new LinkedHashMap<>();
        NodeList fieldNodes = isoElement.getElementsByTagName("field");
        for (int fieldIndex = 0; fieldIndex < fieldNodes.getLength(); fieldIndex++) {
            Node fieldNode = fieldNodes.item(fieldIndex);
            if (!(fieldNode instanceof Element fieldElement)) {
                continue;
            }
            fields.put(fieldElement.getAttribute("id"), fieldElement.getAttribute("value"));
        }
        Object mti = fields.remove("0");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mti", mti);
        body.put("fields", fields);
        return body;
    }

    private String requiredEnv(String name) {
        String value = env(name, "");
        Assumptions.assumeTrue(!value.isBlank(), "Set " + name + " to run the real HPS integration test");
        return value;
    }

    private String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}

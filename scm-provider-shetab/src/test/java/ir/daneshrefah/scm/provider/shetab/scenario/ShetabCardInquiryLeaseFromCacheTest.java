package ir.daneshrefah.scm.provider.shetab.scenario;

import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.client.config.properties.CacheType;
import ir.daneshrefah.scm.cache.client.connector.backend.CacheBackendRouter;
import ir.daneshrefah.scm.cache.client.connector.backend.LocalCaffeineCacheBackend;
import ir.daneshrefah.scm.cache.client.connector.routing.CacheRouteResolver;
import ir.daneshrefah.scm.cache.client.connector.spring.RoutingCacheManager;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.LocalResourceLeaseUtility;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.ResourceLease;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.ResourceLeaseUtility;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.RoutingResourceLeaseUtility;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabIsoMapConverter;
import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShetabCardInquiryLeaseFromCacheTest {

    private static final Path CARD_INQUIRY_SAMPLE_PATH =
            Path.of("/home/cheshmapoush.a@drp.local/Downloads/1100.xml");

    private static final String TEST_RUNTIME_CACHE = "shetab-test-runtime-cache";
    private static final String TEST_HPS_ADDRESS_CACHE = "shetab-test-hps-address-cache";
    private static final String TEST_HPS_ADDRESS_CACHE_KEY = "provider:poya:hps-addresses";
    private static final String TEST_HPS_LEASE_POOL = "shetab-hps-endpoint::poya";

    private static final String CACHE_ADDRESS_KEY = "cache.address";
    private static final String MAC_KEY_CACHE_KEY = "security.mac-key";
    private static final String PIN_KEY_CACHE_KEY = "security.pin-key";

    private static final String CACHE_ADDRESS = "127.0.0.1:5701";
    private static final String MAC_KEY = "3C1D7E9A2B4F6A8C9E0D1F2A3B4C5D6E";
    private static final String PIN_KEY = "9A7C5E3D1B2F4A6C8E0D2F1A3B5C7E9D";
    private static final List<String> HPS_ENDPOINTS = List.of(
            "10.10.10.11:9000",
            "10.10.10.12:9000",
            "10.10.10.13:9000"
    );

    private CacheManager cacheManager;
    private ResourceLeaseUtility resourceLeaseUtility;
    private ShetabIsoMapConverter isoMapConverter;
    private ResourceLease acquiredLease;

    @BeforeEach
    void init() {
        CacheClientProperties cacheProperties = new CacheClientProperties();
        cacheProperties.setDefaultType(CacheType.LOCAL);
        cacheProperties.getCaches().put(TEST_RUNTIME_CACHE, localCacheDefinition());
        cacheProperties.getCaches().put(TEST_HPS_ADDRESS_CACHE, localCacheDefinition());

        CacheRouteResolver routeResolver = new CacheRouteResolver(cacheProperties);
        CacheBackendRouter backendRouter = new CacheBackendRouter(List.of(new LocalCaffeineCacheBackend()));
        cacheManager = new RoutingCacheManager(routeResolver, backendRouter, cacheProperties);

        CacheClientProperties.UtilityBackends utilityBackends = new CacheClientProperties.UtilityBackends();
        utilityBackends.setResourceLease(CacheClientProperties.UtilityBackendType.LOCAL);
        resourceLeaseUtility = new RoutingResourceLeaseUtility(new LocalResourceLeaseUtility(), null, utilityBackends);

        isoMapConverter = new ShetabIsoMapConverter();

        // runtime variables needed for test execution
        putInCache(TEST_RUNTIME_CACHE, CACHE_ADDRESS_KEY, CACHE_ADDRESS);
        putInCache(TEST_RUNTIME_CACHE, MAC_KEY_CACHE_KEY, MAC_KEY);
        putInCache(TEST_RUNTIME_CACHE, PIN_KEY_CACHE_KEY, PIN_KEY);

        // initialize cache with HPS endpoints that should be leased as ip:port
        putInCache(TEST_HPS_ADDRESS_CACHE, TEST_HPS_ADDRESS_CACHE_KEY, HPS_ENDPOINTS);
    }

    @AfterEach
    void cleanup() {
        if (acquiredLease != null) {
            acquiredLease.close();
        }
    }

    @Test
    void leasesHpsAddressFromCacheAndBuildsCardInquiryIsoMessages() throws Exception {
        Assumptions.assumeTrue(Files.exists(CARD_INQUIRY_SAMPLE_PATH),
                "Sample file does not exist: " + CARD_INQUIRY_SAMPLE_PATH);

        String cachedAddress = getFromCache(TEST_RUNTIME_CACHE, CACHE_ADDRESS_KEY, String.class);
        String cachedMacKey = getFromCache(TEST_RUNTIME_CACHE, MAC_KEY_CACHE_KEY, String.class);
        String cachedPinKey = getFromCache(TEST_RUNTIME_CACHE, PIN_KEY_CACHE_KEY, String.class);
        assertEquals(CACHE_ADDRESS, cachedAddress);
        assertEquals(MAC_KEY, cachedMacKey);
        assertEquals(PIN_KEY, cachedPinKey);

        @SuppressWarnings("unchecked")
        List<String> cachedHpsEndpoints =
                (List<String>) getFromCache(TEST_HPS_ADDRESS_CACHE, TEST_HPS_ADDRESS_CACHE_KEY, List.class);
        assertEquals(HPS_ENDPOINTS, cachedHpsEndpoints);

        acquiredLease = resourceLeaseUtility.acquire(
                TEST_HPS_LEASE_POOL,
                cachedHpsEndpoints,
                Duration.ofSeconds(45)
        );
        String leasedEndpoint = acquiredLease.resourceName();
        assertTrue(leasedEndpoint.matches("\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+"));
        assertTrue(cachedHpsEndpoints.contains(leasedEndpoint));

        EndpointParts endpointParts = parseEndpoint(leasedEndpoint);
        assertFalse(endpointParts.host().isBlank());
        assertTrue(endpointParts.port() > 0);

        List<Map<String, Object>> isoBodies = readIsoBodies(CARD_INQUIRY_SAMPLE_PATH);
        Map<String, Object> request1100 = findByMti(isoBodies, "1100");
        Map<String, Object> response1110 = findByMti(isoBodies, "1110");

        ISOMsg requestIso = isoMapConverter.toIsoMsg(request1100);
        ISOMsg responseIso = isoMapConverter.toIsoMsg(response1110);

        assertEquals("1100", requestIso.getMTI());
        assertEquals("5894631159226349", requestIso.getString(2));
        assertEquals("330000", requestIso.getString(3));
        assertEquals("261655", requestIso.getString(11));
        assertEquals("691199261655", requestIso.getString(37));

        assertEquals("1110", responseIso.getMTI());
        assertEquals("118", responseIso.getString(39));
        assertEquals("261655", responseIso.getString(11));
        assertEquals("691199261655", responseIso.getString(37));
    }

    private CacheClientProperties.CacheDefinition localCacheDefinition() {
        CacheClientProperties.CacheDefinition cacheDefinition = new CacheClientProperties.CacheDefinition();
        cacheDefinition.setType(CacheType.LOCAL);
        return cacheDefinition;
    }

    private void putInCache(String cacheName, String key, Object value) {
        cache(cacheName).put(key, value);
    }

    private <T> T getFromCache(String cacheName, String key, Class<T> valueType) {
        return cache(cacheName).get(key, valueType);
    }

    private Cache cache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalStateException("Spring cache is not configured: " + cacheName);
        }
        return cache;
    }

    private List<Map<String, Object>> readIsoBodies(Path xmlPath) throws Exception {
        String xmlContent = Files.readString(xmlPath, StandardCharsets.UTF_8);
        String wrappedXml = "<root>" + xmlContent + "</root>";
        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(wrappedXml.getBytes(StandardCharsets.UTF_8)));

        List<Map<String, Object>> result = new ArrayList<>();
        NodeList isoNodes = document.getElementsByTagName("isomsg");
        for (int index = 0; index < isoNodes.getLength(); index++) {
            Node node = isoNodes.item(index);
            if (!(node instanceof Element isoElement)) {
                continue;
            }
            Map<String, String> fields = new LinkedHashMap<>();
            NodeList fieldNodes = isoElement.getElementsByTagName("field");
            for (int fieldIndex = 0; fieldIndex < fieldNodes.getLength(); fieldIndex++) {
                Node fieldNode = fieldNodes.item(fieldIndex);
                if (!(fieldNode instanceof Element fieldElement)) {
                    continue;
                }
                String id = fieldElement.getAttribute("id");
                String value = fieldElement.getAttribute("value");
                fields.put(id, value);
            }
            String mti = fields.remove("0");
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mti", mti);
            body.put("fields", fields);
            result.add(body);
        }
        return result;
    }

    private Map<String, Object> findByMti(List<Map<String, Object>> bodies, String mti) {
        for (Map<String, Object> body : bodies) {
            if (mti.equals(body.get("mti"))) {
                return body;
            }
        }
        throw new IllegalStateException("Could not find ISO body with MTI=" + mti);
    }

    private EndpointParts parseEndpoint(String endpoint) {
        int separator = endpoint.lastIndexOf(':');
        if (separator <= 0 || separator == endpoint.length() - 1) {
            throw new IllegalStateException("Invalid endpoint format: " + endpoint);
        }
        String host = endpoint.substring(0, separator).trim();
        int port = Integer.parseInt(endpoint.substring(separator + 1).trim());
        return new EndpointParts(host, port);
    }

    private record EndpointParts(String host, int port) {
    }
}

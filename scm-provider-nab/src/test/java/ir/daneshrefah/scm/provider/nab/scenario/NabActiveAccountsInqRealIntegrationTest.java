package ir.daneshrefah.scm.provider.nab.scenario;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryRegistry;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipelineFactory;
import ir.daneshrefah.scm.provider.nab.application.NabProviderService;
import ir.daneshrefah.scm.provider.nab.codec.FixedLengthDecoder;
import ir.daneshrefah.scm.provider.nab.codec.FixedLengthEncoder;
import ir.daneshrefah.scm.provider.nab.codec.JsonFieldSpecReader;
import ir.daneshrefah.scm.provider.nab.codec.NabCommandSpecReader;
import ir.daneshrefah.scm.provider.nab.codec.NabHeaderResolver;
import ir.daneshrefah.scm.provider.nab.codec.NabProtocolHeaderBuilder;
import ir.daneshrefah.scm.provider.nab.codec.NabResponseParser;
import ir.daneshrefah.scm.provider.nab.codec.NabResponseSpecReader;
import ir.daneshrefah.scm.provider.nab.codec.NabRqUidGenerator;
import ir.daneshrefah.scm.provider.nab.codec.NabTextNormalizer;
import ir.daneshrefah.scm.provider.nab.codec.NabValueConverterRegistry;
import ir.daneshrefah.scm.provider.nab.codec.PersianDateFormatter;
import ir.daneshrefah.scm.provider.nab.config.NabConfigResolver;
import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import ir.daneshrefah.scm.provider.nab.metrics.NabProviderMetrics;
import ir.daneshrefah.scm.provider.nab.tcp.NabPooledTcpClient;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
class NabActiveAccountsInqRealIntegrationTest {
    private static final String DEFAULT_ENDPOINT = "10.15.27.12:3080";
    private static final String DEFAULT_USER_ID = "999998";
    private static final String DEFAULT_PASSWORD = "1234567890";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void sendsActiveAccountsInquiryToRealNab() {
        Assumptions.assumeTrue(Boolean.parseBoolean(env("SCM_NAB_INTEGRATION", "false")),
                "Set SCM_NAB_INTEGRATION=true to run the real NAB integration test");

        String customerId = env("SCM_NAB_ACTIVE_ACCOUNTS_CUSTOMER_ID", "3782173");
        String generalAccount = env("SCM_NAB_ACTIVE_ACCOUNTS_GENERAL_ACCOUNT", "");
        NabResolvedConfig config = config();

        NabProviderService providerService = providerService();
        ObjectNode response = providerService.execute(request(customerId, generalAccount, "ATPI"), config);
        response = providerService.execute(request(customerId, generalAccount, "ATPI"), config);

        JsonNode status = response.path("status");
        assertTrue(status.isObject(), "response.status must be present");

        String actionCode = status.path("code").asText("");
        assertFalse(actionCode.isBlank(), "response.status.code must not be blank");
        assertEquals("27", response.path("command").asText());
        assertEquals("ATPI", response.path("protocol").asText());
        assertEquals(16, response.path("rqUid").asText("").length(), "rqUid should be length 16");
        assertTrue(status.path("success").asBoolean(false),
                "NAB returned non-success actionCode=" + actionCode);

        if (status.path("list").asBoolean(false)) {
            JsonNode records = response.path("records");
            assertTrue(records.isArray(), "response.records must be array for list response");
            if (!records.isEmpty()) {
                JsonNode first = records.get(0);
                assertTrue(first.has("accountNo"));
                assertTrue(first.has("customerId"));
                assertTrue(first.has("accountIban"));
            }
        } else {
            JsonNode data = response.path("data");
            assertTrue(data.isObject(), "response.data must be object for non-list response");
            assertTrue(data.has("accountNo"));
            assertTrue(data.has("customerId"));
        }
    }

    private NabResolvedConfig config() {
        ProviderRegistryProperties registry = new ProviderRegistryProperties();
        registry.put("core", providerConfig());
        return new NabConfigResolver(registry, new ProviderMessageCustomizerPipelineFactory(
                new ProviderMessageCustomizerFactoryRegistry(List.of()))).resolve("core", null);
    }

    private Map<String, Object> providerConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("scheme", "scm-nab");
        config.put("protocol", "ATPI");
        config.put("endpoint", endpoint());
        config.put("charset", "windows-1256");
        config.put("connect-timeout-ms", intEnv("SCM_NAB_CONNECT_TIMEOUT_MS", 3000));
        config.put("socket-timeout-ms", intEnv("SCM_NAB_SOCKET_TIMEOUT_MS", 1000));
        config.put("response-timeout-ms", intEnv("SCM_NAB_RESPONSE_TIMEOUT_MS", 10000));
        config.put("response-idle-timeout-ms", intEnv("SCM_NAB_RESPONSE_IDLE_TIMEOUT_MS", 200));
        config.put("default-service-code", "99");
        config.put("wire-log-enabled", Boolean.parseBoolean(env("SCM_NAB_WIRE_LOG_ENABLED", "true")));
        config.put("user-id", env("SCM_NAB_USER_ID", DEFAULT_USER_ID));
        config.put("password", env("SCM_NAB_PASSWORD", DEFAULT_PASSWORD));
        config.put("service-codes-by-terminal-type", Map.of("ATM", "00"));
        config.put("service-codes-by-channel-code", Map.of("MB", "03"));
        config.put("header-fields", List.of(
                field("protocol", 4, true),
                field("clientAddress", 64, true),
                field("command", 2, true),
                field("serviceCode", 2, true),
                field("dateTime", 14, true),
                field("userId", 10, true),
                field("password", 10, true),
                field("rqUid", 16, true)
        ));
        return config;
    }

    private Map<String, Object> field(String name, int length, boolean required) {
        return Map.of("name", name, "length", length, "required", required);
    }

    private String endpoint() {
        String endpoint = env("SCM_NAB_ENDPOINT", null);
        if (endpoint != null) {
            return endpoint;
        }
        String endpoints = env("SCM_NAB_ENDPOINTS", DEFAULT_ENDPOINT);
        if (endpoints.contains(",")) {
            throw new IllegalArgumentException("SCM_NAB_ENDPOINTS supports one endpoint only; use SCM_NAB_ENDPOINT");
        }
        return endpoints.trim();
    }

    private ObjectNode request(String customerId, String generalAccount, String protocol) {
        ObjectNode root = objectMapper.createObjectNode();

        ObjectNode command = root.putObject("command");
        command.put("code", "27");
        command.put("protocol", protocol);

        ObjectNode header = root.putObject("header");
        header.put("terminalType", env("SCM_NAB_TERMINAL_TYPE", "ATM"));
        header.put("channelCode", env("SCM_NAB_CHANNEL_CODE", "MB"));
        header.put("clientAddress", env("SCM_NAB_CLIENT_ADDRESS", "127.0.0.1"));

        ObjectNode data = root.putObject("data");
        data.put("customerId", customerId);
        data.put("generalAccount", generalAccount);

        ObjectNode request = root.putObject("request");
        ArrayNode requestFields = request.putArray("fields");
        addField(requestFields, "customerId", 12, true);
        addField(requestFields, "generalAccount", 8, false);

        ObjectNode response = root.putObject("response");
        ObjectNode status = response.putObject("status");
        ObjectNode statusField = status.putObject("field");
        statusField.put("name", "actionCode");
        statusField.put("length", 5);
        status.put("successCode", "00000");
        status.put("successListCode", "10000");
        response.put("recordSeparator", "\n");

        ArrayNode responseFields = response.putArray("fields");
        addField(responseFields, "command", 2);
        addField(responseFields, "service", 2);
        addField(responseFields, "dateTime", 14);
        addField(responseFields, "accountNo", 18);
        addField(responseFields, "accountType", 2);
        addField(responseFields, "accountDesc", 60);
        addField(responseFields, "accountLedgerBalance", 18);
        addField(responseFields, "accountAvailableBalance", 18);
        addField(responseFields, "branchNo", 6);
        addField(responseFields, "customerId", 12);
        addField(responseFields, "customerType", 2);
        addField(responseFields, "sharingStatus", 1);
        addField(responseFields, "rqUid", 16);
        addField(responseFields, "lastUpdateTimeMs", 17);
        addField(responseFields, "microSec", 3);
        addField(responseFields, "accountStatus", 2);
        addField(responseFields, "accountBlockedAmount", 18);
        addField(responseFields, "accountIban", 30);
        addField(responseFields, "commercial", 1);
        addField(responseFields, "generalAccount", 8);
        addField(responseFields, "generalAccountDesc", 60);
        addField(responseFields, "subsidiary", 8);
        addField(responseFields, "subsidiaryDesc", 60);
        addField(responseFields, "isColorMoney", 1);
        return root;
    }

    private void addField(ArrayNode fields, String name, int length) {
        addField(fields, name, length, false);
    }

    private void addField(ArrayNode fields, String name, int length, boolean required) {
        ObjectNode field = fields.addObject();
        field.put("name", name);
        field.put("length", length);
        if (required) {
            field.put("required", true);
        }
    }

    private NabProviderService providerService() {
        NabValueConverterRegistry converters = new NabValueConverterRegistry();
        JsonFieldSpecReader fieldReader = new JsonFieldSpecReader();
        FixedLengthEncoder encoder = new FixedLengthEncoder(converters);
        FixedLengthDecoder decoder = new FixedLengthDecoder(objectMapper, converters);
        return new NabProviderService(
                new NabCommandSpecReader(),
                fieldReader,
                new NabResponseSpecReader(fieldReader),
                new NabHeaderResolver(new NabRqUidGenerator(), new PersianDateFormatter()),
                encoder,
                new NabProtocolHeaderBuilder(encoder),
                new NabPooledTcpClient(new NabTextNormalizer(), new NabProviderMetrics()),
                new NabResponseParser(objectMapper, decoder)
        );
    }

    private int intEnv(String key, int defaultValue) {
        String value = env(key, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid integer for env " + key + ": " + value, ex);
        }
    }

    private String requiredEnv(String key) {
        String value = env(key, "");
        Assumptions.assumeTrue(!value.isBlank(), "Set " + key + " to run the real NAB integration test");
        return value;
    }

    private String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}

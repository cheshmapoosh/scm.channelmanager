package ir.daneshrefah.scm.provider.nab.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryRegistry;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipelineFactory;
import ir.daneshrefah.scm.provider.nab.config.NabConfigResolver;
import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import ir.daneshrefah.scm.provider.nab.metrics.NabProviderMetrics;
import ir.daneshrefah.scm.provider.nab.tcp.NabPooledTcpClient;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NabProviderServiceTcpIntegrationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void callsNabOverTcpAndParsesListResponse() throws Exception {
        Charset charset = Charset.forName("windows-1252");
        List<String> responseFrames = List.of(
                "1000000000000000012345610",
                "1000000000000000065432120",
                "00000"
        );

        try (FakeNabServer server = new FakeNabServer(charset, 134, responseFrames)) {
            NabResolvedConfig config = config(server.endpoint(), "ATPI");
            ObjectNode result = providerService().execute(request(), config);

            server.await();
            assertEquals("ATPI", server.protocol());
            assertEquals(134, server.body().length());
            assertTrue(result.get("status").get("success").asBoolean());
            assertEquals(2, result.get("records").size());
            assertEquals("000000000000123456", result.get("records").get(0).get("accountNo").asText());
            assertEquals("20", result.get("records").get(1).get("accountType").asText());
        }
    }

    @Test
    void opensNewConnectionForSequentialRequestsBecauseNabIsSingleCommandPerConnection() throws Exception {
        Charset charset = Charset.forName("windows-1252");
        String response = "00000";

        try (FakeNabServer server = new FakeNabServer(charset, 134, response, 2)) {
            NabProviderService providerService = providerService();
            NabResolvedConfig config = config(server.endpoint(), "ATPI");
            ObjectNode first = providerService.execute(request(), config);
            ObjectNode second = providerService.execute(request(), config);

            server.await();
            assertEquals(2, server.acceptedSockets());
            assertEquals(2, server.requestCount());
            assertTrue(first.get("status").get("success").asBoolean());
            assertTrue(second.get("status").get("success").asBoolean());
        }
    }

    @Test
    void opensTwoSocketsForAtpsThenAtpi() throws Exception {
        Charset charset = Charset.forName("windows-1252");
        String atpsBody = "27"
                + "25"
                + "14040822134018"
                + "999998    "
                + "1234567890"
                + "1244422         "
                + "3782173     "
                + "        ";

        try (FakeNabServer server = new FakeNabServer(
                charset,
                List.of(atpsBody.length(), 134),
                List.of("00000"),
                2)) {
            NabProviderService providerService = providerService();
            NabResolvedConfig atpsConfig = config(server.endpoint(), "ATPS");
            NabResolvedConfig atpiConfig = config(server.endpoint(), "ATPI");

            ObjectNode atpsResult = providerService.execute(atpsSampleRequest(), atpsConfig);
            ObjectNode atpiResult = providerService.execute(request(), atpiConfig);

            server.await();
            assertEquals(2, server.acceptedSockets());
            assertEquals(2, server.requestCount());
            assertEquals("ATPS", server.protocolAt(0));
            assertEquals("ATPI", server.protocolAt(1));
            assertEquals(atpsBody, server.bodyAt(0));
            assertEquals(134, server.bodyAt(1).length());
            assertTrue(atpsResult.get("status").get("success").asBoolean());
            assertTrue(atpiResult.get("status").get("success").asBoolean());
        }
    }

    @Test
    void atpiRequiresClientAddressHeader() throws Exception {
        ObjectNode input = request();
        ((ObjectNode) input.get("header")).remove("clientAddress");
        NabResolvedConfig config = config("127.0.0.1:1", "ATPI");

        assertThrows(IllegalArgumentException.class, () -> providerService().execute(input, config));
    }

    @Test
    void providerInstanceCanBeRestrictedToSingleProtocol() throws Exception {
        NabResolvedConfig config = config("127.0.0.1:1", "ATPI");
        assertThrows(IllegalArgumentException.class, () -> providerService().execute(atpsSampleRequest(), config));
    }

    @Test
    void buildsAtpsWireBodyFromLegacySampleValues() throws Exception {
        Charset charset = Charset.forName("windows-1252");
        String response = "00000";

        String expectedBody = "27"
                + "25"
                + "14040822134018"
                + "999998    "
                + "1234567890"
                + "1244422         "
                + "3782173     "
                + "        ";

        try (FakeNabServer server = new FakeNabServer(charset, expectedBody.length(), response)) {
            NabResolvedConfig config = config(server.endpoint(), "ATPS");
            ObjectNode result = providerService().execute(atpsSampleRequest(), config);

            server.await();
            assertEquals("ATPS", server.protocol());
            assertEquals(expectedBody.length(), server.body().length());
            assertEquals(expectedBody, server.body());
            assertTrue(result.get("status").get("success").asBoolean());
            assertFalse(result.get("status").get("list").asBoolean());
        }
    }

    private ObjectNode request() throws Exception {
        return (ObjectNode) objectMapper.readTree("""
                {
                  "command": {
                    "code": "27",
                    "protocol": "ATPI"
                  },
                  "header": {
                    "terminalType": "ATM",
                    "channelCode": "MOBILE",
                    "clientAddress": "10.1.1.10"
                  },
                  "data": {
                    "customerId": "123456",
                    "generalAccount": "0"
                  },
                  "request": {
                    "fields": [
                      {"name": "customerId", "length": 12, "required": true},
                      {"name": "generalAccount", "length": 4}
                    ]
                  },
                  "response": {
                    "recordSeparator": "\\n",
                    "fields": [
                      {"name": "accountNo", "length": 18},
                      {"name": "accountType", "length": 2}
                    ]
                  }
                }
                """);
    }

    private ObjectNode atpsSampleRequest() throws Exception {
        return (ObjectNode) objectMapper.readTree("""
                {
                  "command": {
                    "code": "27",
                    "protocol": "ATPS"
                  },
                  "header": {
                    "serviceCode": "25",
                    "dateTime": "14040822134018",
                    "userId": "999998",
                    "password": "1234567890",
                    "rqUid": "1244422"
                  },
                  "data": {
                    "customerId": "3782173",
                    "generalAccount": ""
                  },
                  "request": {
                    "fields": [
                      {"name": "customerId", "length": 12, "required": true},
                      {"name": "generalAccount", "length": 8}
                    ]
                  },
                  "response": {
                    "fields": []
                  }
                }
                """);
    }

    private NabResolvedConfig config(String endpoint) {
        return config(endpoint, "ATPI");
    }

    private NabResolvedConfig config(String endpoint, String protocol) {
        ProviderRegistryProperties registry = new ProviderRegistryProperties();
        registry.put("core", providerConfig(endpoint, protocol == null ? "ATPI" : protocol));
        return new NabConfigResolver(registry, new ProviderMessageCustomizerPipelineFactory(
                new ProviderMessageCustomizerFactoryRegistry(List.of()))).resolve("core", null);
    }

    private Map<String, Object> providerConfig(String endpoint, String protocol) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("scheme", "scm-nab");
        config.put("protocol", protocol);
        config.put("endpoint", endpoint);
        config.put("charset", "windows-1252");
        config.put("response-timeout-ms", 2000);
        config.put("response-idle-timeout-ms", 50);
        config.put("user-id", "999998");
        config.put("password", "1234567890");
        config.put("default-service-code", "99");
        config.put("service-codes-by-terminal-type", Map.of("ATM", "01"));
        config.put("header-fields-by-protocol", headerFieldsByProtocol());
        return config;
    }

    private Map<String, Object> headerFieldsByProtocol() {
        return Map.of(
                "ATPI", List.of(
                        field("protocol", 4, true),
                        field("clientAddress", 64, true),
                        field("command", 2, true),
                        field("serviceCode", 2, true),
                        field("dateTime", 14, true),
                        field("userId", 10, true),
                        field("password", 10, true),
                        field("rqUid", 16, true)
                ),
                "ATPS", List.of(
                        field("protocol", 4, true),
                        field("command", 2, true),
                        field("serviceCode", 2, true),
                        field("dateTime", 14, true),
                        field("userId", 10, true),
                        field("password", 10, true),
                        field("rqUid", 16, true)
                )
        );
    }

    private Map<String, Object> field(String name, int length, boolean required) {
        return Map.of("name", name, "length", length, "required", required);
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

    private static final class FakeNabServer implements AutoCloseable {
        private final Charset charset;
        private final List<Integer> expectedBodyLengths;
        private final List<String> responseFrames;
        private final int expectedRequestCount;
        private final ServerSocket serverSocket;
        private final CompletableFuture<Void> done;
        private final AtomicInteger acceptedSockets = new AtomicInteger();
        private final AtomicInteger requestCount = new AtomicInteger();
        private final List<String> protocols = Collections.synchronizedList(new ArrayList<>());
        private final List<String> bodies = Collections.synchronizedList(new ArrayList<>());
        private volatile String protocol;
        private volatile String body;

        private FakeNabServer(Charset charset, int expectedBodyLength, String response) throws Exception {
            this(charset, List.of(expectedBodyLength), List.of(response), 1);
        }

        private FakeNabServer(Charset charset, int expectedBodyLength, List<String> responseFrames) throws Exception {
            this(charset, List.of(expectedBodyLength), responseFrames, 1);
        }

        private FakeNabServer(Charset charset, int expectedBodyLength, String response, int expectedRequestCount) throws Exception {
            this(charset, repeatedLengths(expectedBodyLength, expectedRequestCount), List.of(response), expectedRequestCount);
        }

        private FakeNabServer(Charset charset, List<Integer> expectedBodyLengths, List<String> responseFrames, int expectedRequestCount) throws Exception {
            this.charset = charset;
            this.expectedBodyLengths = List.copyOf(expectedBodyLengths);
            this.responseFrames = List.copyOf(responseFrames);
            this.expectedRequestCount = expectedRequestCount;
            this.serverSocket = new ServerSocket(0);
            this.done = CompletableFuture.runAsync(this::serve);
        }

        String endpoint() {
            return "127.0.0.1:" + serverSocket.getLocalPort();
        }

        String protocol() {
            return protocol;
        }

        String body() {
            return body;
        }

        String protocolAt(int index) {
            return protocols.get(index);
        }

        String bodyAt(int index) {
            return bodies.get(index);
        }

        int acceptedSockets() {
            return acceptedSockets.get();
        }

        int requestCount() {
            return requestCount.get();
        }

        void await() throws Exception {
            done.get(3, TimeUnit.SECONDS);
        }

        private void serve() {
            try {
                for (int i = 0; i < expectedRequestCount; i++) {
                    try (Socket socket = serverSocket.accept()) {
                        acceptedSockets.incrementAndGet();
                        BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
                        BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream());

                        protocol = new String(input.readNBytes(4), charset);
                        protocols.add(protocol);
                        output.write(frame("00000"));
                        output.flush();

                        int expectedLength = expectedBodyLengths.get(i);
                        body = new String(input.readNBytes(expectedLength), charset);
                        bodies.add(body);
                        requestCount.incrementAndGet();
                        for (String responseFrame : responseFrames) {
                            output.write(frame(responseFrame));
                            output.flush();
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private byte[] frame(String payload) {
            String safePayload = payload == null ? "" : payload;
            String length = String.format("%05d", safePayload.getBytes(charset).length);
            return (length + safePayload).getBytes(charset);
        }

        private static List<Integer> repeatedLengths(int length, int count) {
            List<Integer> values = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                values.add(length);
            }
            return values;
        }

        @Override
        public void close() throws Exception {
            serverSocket.close();
        }
    }
}

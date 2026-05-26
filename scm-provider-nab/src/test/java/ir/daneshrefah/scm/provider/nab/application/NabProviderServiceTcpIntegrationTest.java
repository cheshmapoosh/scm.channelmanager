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
import ir.daneshrefah.scm.provider.nab.config.NabConfigResolver;
import ir.daneshrefah.scm.provider.nab.config.NabProperties;
import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import ir.daneshrefah.scm.provider.nab.tcp.NabConnectionPoolRegistry;
import ir.daneshrefah.scm.provider.nab.tcp.NabPooledTcpClient;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NabProviderServiceTcpIntegrationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void callsNabOverTcpAndParsesListResponse() throws Exception {
        Charset charset = Charset.forName("windows-1252");
        String response = "1000000000000000012345610\n"
                + "1000000000000000065432120";

        try (FakeNabServer server = new FakeNabServer(charset, 134, response)) {
            NabResolvedConfig config = config(server.endpoint());
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
    void reusesOneConnectionForSequentialRequestsWhenPoolSizeIsOne() throws Exception {
        Charset charset = Charset.forName("windows-1252");
        String response = "1000000000000000012345610";

        try (FakeNabServer server = new FakeNabServer(charset, 134, response, 2);
             NabConnectionPoolRegistry registry = new NabConnectionPoolRegistry()) {
            NabProviderService providerService = providerService(registry);
            NabResolvedConfig config = config(server.endpoint());
            ObjectNode first = providerService.execute(request(), config);
            ObjectNode second = providerService.execute(request(), config);

            server.await();
            assertEquals(1, server.acceptedSockets());
            assertEquals(2, server.requestCount());
            assertTrue(first.get("status").get("success").asBoolean());
            assertTrue(second.get("status").get("success").asBoolean());
        }
    }

    @Test
    void atpiRequiresClientAddressHeader() throws Exception {
        ObjectNode input = request();
        ((ObjectNode) input.get("header")).remove("clientAddress");
        NabResolvedConfig config = config("127.0.0.1:1");

        assertThrows(IllegalArgumentException.class, () -> providerService().execute(input, config));
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

    private NabResolvedConfig config(String endpoint) {
        NabProperties properties = new NabProperties();
        properties.getDefaults().setCharset("windows-1252");
        properties.getDefaults().setResponseTimeoutMs(2000);
        properties.getDefaults().setResponseIdleTimeoutMs(50);
        properties.getDefaults().getServiceCodesByTerminalType().put("ATM", "01");

        NabProperties.Instance core = new NabProperties.Instance();
        core.setEndpoints(List.of(endpoint));
        core.setUserId("999998");
        core.setPassword("1234567890");
        core.getConnectionPool().setMaxSize(1);
        core.getConnectionPool().setMaxIdle(1);
        core.getConnectionPool().setBorrowTimeoutMs(500);
        properties.getProviders().put("core", core);

        return new NabConfigResolver(properties).resolve("core", null);
    }

    private NabProviderService providerService() {
        return providerService(new NabConnectionPoolRegistry());
    }

    private NabProviderService providerService(NabConnectionPoolRegistry poolRegistry) {
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
                new NabPooledTcpClient(new NabTextNormalizer(), poolRegistry),
                new NabResponseParser(objectMapper, decoder)
        );
    }

    private static final class FakeNabServer implements AutoCloseable {
        private final Charset charset;
        private final int expectedBodyLength;
        private final String response;
        private final int expectedRequestCount;
        private final ServerSocket serverSocket;
        private final CompletableFuture<Void> done;
        private final AtomicInteger acceptedSockets = new AtomicInteger();
        private final AtomicInteger requestCount = new AtomicInteger();
        private volatile String protocol;
        private volatile String body;

        private FakeNabServer(Charset charset, int expectedBodyLength, String response) throws Exception {
            this(charset, expectedBodyLength, response, 1);
        }

        private FakeNabServer(Charset charset, int expectedBodyLength, String response, int expectedRequestCount) throws Exception {
            this.charset = charset;
            this.expectedBodyLength = expectedBodyLength;
            this.response = response;
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
            try (Socket socket = serverSocket.accept()) {
                acceptedSockets.incrementAndGet();
                BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
                BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream());

                for (int i = 0; i < expectedRequestCount; i++) {
                    protocol = new String(input.readNBytes(4), charset);
                    output.write("00000".getBytes(charset));
                    output.flush();

                    body = new String(input.readNBytes(expectedBodyLength), charset);
                    requestCount.incrementAndGet();
                    output.write(response.getBytes(charset));
                    output.flush();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() throws Exception {
            serverSocket.close();
        }
    }
}

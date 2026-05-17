package ir.daneshrefah.scm.provider.rest.scenario;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import ir.daneshrefah.scm.cache.client.utility.lock.LockUtility;
import ir.daneshrefah.scm.provider.rest.camel.RestProviderComponent;
import ir.daneshrefah.scm.provider.rest.camel.RestProviderEndpoint;
import ir.daneshrefah.scm.provider.rest.config.RestProviderConfigResolver;
import ir.daneshrefah.scm.provider.rest.config.RestProviderHeaders;
import ir.daneshrefah.scm.provider.rest.config.RestProviderProperties;
import ir.daneshrefah.scm.provider.rest.http.RestProviderClientRegistry;
import ir.daneshrefah.scm.provider.rest.log.RestProviderLogSanitizer;
import ir.daneshrefah.scm.provider.rest.metrics.RestProviderMetrics;
import ir.daneshrefah.scm.provider.rest.token.RestProviderTokenManager;
import ir.daneshrefah.scm.provider.rest.trace.RestProviderTraceSupport;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.SimpleRegistry;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
class RestProviderHpsCardInquiryIntegrationTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Test
    void sendsCardInquiryToRealHpsServiceThroughRestProviderProducer() throws Exception {
        Assumptions.assumeTrue(
                Boolean.parseBoolean(env("SCM_REST_HPS_INTEGRATION", "false")),
                "Set SCM_REST_HPS_INTEGRATION=true to run the real HPS REST integration test"
        );

        String baseUrl = env("SCM_REST_HPS_BASE_URL", "http://10.15.1.61:9677");
        String username = env("SCM_REST_HPS_USERNAME", "user01");
        String password = env("SCM_REST_HPS_PASSWORD", "pass01");
        String endpointPath = env("SCM_REST_HPS_PATH", "/general/ws/do");

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
             CamelContext camelContext = createCamelContext(baseUrl, username, password, executor)) {
            camelContext.start();

            RestProviderEndpoint endpoint = camelContext.getEndpoint("rest-provider:hpsRest", RestProviderEndpoint.class);
            Producer producer = endpoint.createProducer();
            producer.start();
            try {
                Exchange exchange = endpoint.createExchange(ExchangePattern.InOut);
                exchange.getMessage().setHeader(RestProviderHeaders.METHOD, "POST");
                exchange.getMessage().setHeader(RestProviderHeaders.PATH, endpointPath);
                exchange.getMessage().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                exchange.getMessage().setBody(sampleRequestBody());

                producer.process(exchange);

                Integer statusCode = exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
                assertNotNull(statusCode, "REST provider should set HTTP response code");
                assertTrue(statusCode >= 200 && statusCode < 300,
                        "Expected 2xx status from HPS card inquiry endpoint, but got " + statusCode);

                Map<?, ?> payload = exchange.getMessage().getBody(Map.class);
                assertNotNull(payload, "REST provider response payload should not be null");
                Object responseBody = payload.get("body");
                if (responseBody instanceof String text) {
                    assertFalse(text.isBlank(), "HPS response body should not be blank");
                } else {
                    assertNotNull(responseBody, "HPS response body should not be null");
                }
            } finally {
                producer.stop();
            }
        }
    }

    private CamelContext createCamelContext(String baseUrl, String username, String password, ExecutorService executor) {
        ObjectMapper objectMapper = new ObjectMapper();
        RestProviderProperties properties = properties(baseUrl, username, password);
        RestProviderConfigResolver configResolver = new RestProviderConfigResolver(properties);

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("restProviderVirtualThreadExecutor", executor);

        ObjectProvider<ExecutorService> executorProvider = beanFactory.getBeanProvider(ExecutorService.class);
        ObjectProvider<CacheManager> cacheManagerProvider = beanFactory.getBeanProvider(CacheManager.class);
        ObjectProvider<LockUtility> lockUtilityProvider = beanFactory.getBeanProvider(LockUtility.class);

        RestProviderClientRegistry clientRegistry = new RestProviderClientRegistry(executorProvider);
        RestProviderMetrics metrics = new RestProviderMetrics();
        RestProviderTraceSupport traceSupport = new RestProviderTraceSupport(
                OpenTelemetry.noop().getTracer("scm-provider-rest-test")
        );
        RestProviderLogSanitizer logSanitizer = new RestProviderLogSanitizer(objectMapper);
        RestProviderTokenManager tokenManager = new RestProviderTokenManager(
                clientRegistry,
                objectMapper,
                cacheManagerProvider,
                lockUtilityProvider,
                metrics
        );

        SimpleRegistry registry = new SimpleRegistry();
        registry.bind("restProviderConfigResolver", configResolver);
        registry.bind("restProviderClientRegistry", clientRegistry);
        registry.bind("restProviderMetrics", metrics);
        registry.bind("restProviderTraceSupport", traceSupport);
        registry.bind("restProviderLogSanitizer", logSanitizer);
        registry.bind("restProviderTokenManager", tokenManager);
        registry.bind("objectMapper", objectMapper);

        DefaultCamelContext camelContext = new DefaultCamelContext(registry);
        camelContext.addComponent("rest-provider", new RestProviderComponent(camelContext));
        return camelContext;
    }

    private RestProviderProperties properties(String baseUrl, String username, String password) {
        RestProviderProperties properties = new RestProviderProperties();

        RestProviderProperties.Instance instance = new RestProviderProperties.Instance();
        instance.setBaseUrl(baseUrl);
        instance.setConnectTimeoutMs(Integer.parseInt(env("SCM_REST_HPS_CONNECT_TIMEOUT_MS", "3000")));
        instance.setResponseTimeoutMs(Integer.parseInt(env("SCM_REST_HPS_RESPONSE_TIMEOUT_MS", "10000")));
        instance.setDefaultMethod("POST");
        instance.setHeaders(Map.of());

        RestProviderProperties.Auth auth = new RestProviderProperties.Auth();
        auth.setType("BASIC");
        auth.setHeaderName(HttpHeaders.AUTHORIZATION);
        auth.setUsername(username);
        auth.setPassword(password);
        auth.setBasicBase64(true);
        instance.setAuth(auth);

        RestProviderProperties.Security security = new RestProviderProperties.Security();
        security.setSensitiveHeaders(List.of("authorization", "proxy-authorization", "cookie", "set-cookie"));
        security.setSensitiveBodyKeys(List.of("password", "token", "secret", "pin", "cvv", "pan", "card"));
        security.setMaxBodyLogLength(400);
        instance.setSecurity(security);

        RestProviderProperties.Token token = new RestProviderProperties.Token();
        token.setEnabled(false);
        instance.setToken(token);

        properties.getProviders().put("hpsRest", instance);
        return properties;
    }

    private Map<String, Object> sampleRequestBody() {
        String stan = env("SCM_REST_HPS_STAN", String.format("%06d", System.currentTimeMillis() % 1_000_000));
        String reference = env("SCM_REST_HPS_REFERENCE", "691199" + stan);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("cardNumber", env("SCM_REST_HPS_CARD_NUMBER", "5894631204245054"));
        data.put("stan", stan);
        data.put("posData", env("SCM_REST_HPS_POS_DATA", "61051061314C"));
        data.put("reference", reference);
        data.put("cvv", env("SCM_REST_HPS_CVV", "639"));
        data.put("expiryDate", env("SCM_REST_HPS_EXPIRY_DATE", "1002"));
        data.put("cardAccTermId", env("SCM_REST_HPS_CARD_ACC_TERM_ID", "67777777"));
        data.put("cardAccId", env("SCM_REST_HPS_CARD_ACC_ID", "   777777777600"));
        data.put("dateAndTime", env("SCM_REST_HPS_DATE_TIME", LocalDateTime.now().format(DATE_TIME_FORMATTER)));
        data.put("cardAccNameAddress", env("SCM_REST_HPS_CARD_ACC_NAME_ADDRESS",
                "Refah Bank            Tehran       THRIR010010157171371502184852851"));
        data.put("destCard", env("SCM_REST_HPS_DEST_CARD", "5894631240208033"));
        data.put("amount", env("SCM_REST_HPS_AMOUNT", "000000000000"));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("serviceName", env("SCM_REST_HPS_SERVICE_NAME", "getCardholderNameCnp"));
        payload.put("data", data);
        return payload;
    }

    private String env(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}

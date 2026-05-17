package ir.daneshrefah.scm.provider.rest.scenario;

import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import ir.daneshrefah.scm.provider.rest.http.RestProviderClientRegistry;
import ir.daneshrefah.scm.provider.rest.model.RestProviderRequestSpec;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
class RestProviderHpsCardInquiryIntegrationTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Test
    void sendsCardInquiryToRealHpsService() {
        Assumptions.assumeTrue(
                Boolean.parseBoolean(env("SCM_REST_HPS_INTEGRATION", "false")),
                "Set SCM_REST_HPS_INTEGRATION=true to run the real HPS REST integration test"
        );

        String baseUrl = env("SCM_REST_HPS_BASE_URL", "http://10.15.1.61:9677");
        String username = env("SCM_REST_HPS_USERNAME", "user01");
        String password = env("SCM_REST_HPS_PASSWORD", "pass01");
        String endpointPath = env("SCM_REST_HPS_PATH", "/general/ws/do");

        Map<String, Object> requestBody = sampleRequestBody();
        Map<String, String> headers = Map.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        RestProviderResolvedConfig config = config(baseUrl, username, password);
        URI uri = URI.create(baseUrl + endpointPath);
        RestProviderRequestSpec requestSpec = new RestProviderRequestSpec(HttpMethod.POST, uri, headers, requestBody);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
            beanFactory.registerSingleton("restProviderVirtualThreadExecutor", executor);
            ObjectProvider<ExecutorService> executorProvider = beanFactory.getBeanProvider(ExecutorService.class);
            RestProviderClientRegistry clientRegistry = new RestProviderClientRegistry(executorProvider);

            ResponseEntity<String> response = clientRegistry.exchange(config, requestSpec);

            assertTrue(response.getStatusCode().is2xxSuccessful(),
                    "Expected 2xx status from HPS card inquiry endpoint, but got " + response.getStatusCode().value());
            assertFalse(response.getBody() == null || response.getBody().isBlank(),
                    "HPS response body should not be blank");
        }
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

    private RestProviderResolvedConfig config(String baseUrl, String username, String password) {
        return new RestProviderResolvedConfig(
                "hpsRest",
                baseUrl,
                3000,
                Integer.parseInt(env("SCM_REST_HPS_RESPONSE_TIMEOUT_MS", "10000")),
                true,
                false,
                RestProviderResolvedConfig.HttpRedirect.NORMAL,
                "POST",
                Map.of(),
                new RestProviderResolvedConfig.Proxy(null, null, null, null),
                new RestProviderResolvedConfig.Auth(
                        RestProviderResolvedConfig.AuthType.BASIC,
                        HttpHeaders.AUTHORIZATION,
                        null,
                        null,
                        username,
                        password,
                        true
                ),
                new RestProviderResolvedConfig.Security(
                        List.of("authorization", "proxy-authorization", "cookie", "set-cookie"),
                        List.of("password", "token", "secret", "pin", "cvv", "pan", "card"),
                        400
                ),
                new RestProviderResolvedConfig.Token(
                        false,
                        "rest_provider_token_cache",
                        "access-token",
                        "rest-provider-token",
                        30,
                        300,
                        "POST",
                        null,
                        null,
                        Map.of(),
                        Map.of(),
                        Map.of(),
                        Map.of(),
                        new RestProviderResolvedConfig.Auth(
                                RestProviderResolvedConfig.AuthType.NONE,
                                HttpHeaders.AUTHORIZATION,
                                null,
                                null,
                                null,
                                null,
                                true
                        ),
                        "access_token",
                        "expires_in",
                        "token_type",
                        "Bearer"
                )
        );
    }

    private String env(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}

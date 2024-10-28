package ir.daneshrefah.scm.otp.config;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

public class RestClientUtils {

    private static final Duration DEFAULT_RESPONSE_TIME_OUT = Duration.ofSeconds(10);
    private static final int DEFAULT_CONNECTION_TIME_OUT_MILLIS = 3000;

    private RestClientUtils() {
    }

    public static RestClient createRestClient(Duration responseTimeout, int connectionTimeoutMillis) {
        return RestClient
                .builder()
                .requestFactory(getClientHttpRequestFactory(responseTimeout, connectionTimeoutMillis))
                .build();
    }

    public static RestClient createRestClient() {
        return RestClient
                .builder()
                .requestFactory(getClientHttpRequestFactory(DEFAULT_RESPONSE_TIME_OUT, DEFAULT_CONNECTION_TIME_OUT_MILLIS))
                .build();
    }

    private static ClientHttpRequestFactory getClientHttpRequestFactory(Duration responseTimeout, int connectionTimeoutMillis) {
        if (responseTimeout == null) {
            responseTimeout = DEFAULT_RESPONSE_TIME_OUT;
        }
        if (connectionTimeoutMillis <= 0) {
            connectionTimeoutMillis = DEFAULT_CONNECTION_TIME_OUT_MILLIS;
        }
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(responseTimeout);
        clientHttpRequestFactory.setConnectionRequestTimeout(connectionTimeoutMillis);
        return clientHttpRequestFactory;
    }
}

package ir.daneshrefah.scm.provider.rest.http;

import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import ir.daneshrefah.scm.provider.rest.model.RestProviderRequestSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestProviderClientRegistry {
    @Qualifier("restProviderVirtualThreadExecutor")
    private final ObjectProvider<ExecutorService> virtualThreadExecutorProvider;
    private final ConcurrentMap<ClientKey, RestClient> clients = new ConcurrentHashMap<>();

    public ResponseEntity<String> exchange(RestProviderResolvedConfig config, RestProviderRequestSpec requestSpec) {
        RestClient client = clients.computeIfAbsent(ClientKey.from(config), ignored -> createClient(config));
        Map<String, String> resolvedHeaders = resolveHeaders(config, requestSpec);
        long startedAt = System.nanoTime();
        log.info(
                "REST SEND provider={} method={} url={} headerCount={} body={}",
                config.provider(),
                requestSpec.method(),
                requestSpec.uri(),
                resolvedHeaders.size(),
                summarizeBody(requestSpec.body())
        );

        try {
            RestClient.RequestBodySpec request = client
                    .method(requestSpec.method())
                    .uri(requestSpec.uri())
                    .headers(headers -> copyHeaders(resolvedHeaders, headers));

            if (requestSpec.body() != null) {
                request.body(requestSpec.body());
            }

            ResponseEntity<String> response = request.retrieve()
                    .onStatus(status -> status.isError(), (clientRequest, clientResponse) -> {
                        // Keep non-2xx responses as normal provider output.
                    })
                    .toEntity(String.class);
            long elapsedMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            log.info(
                    "REST RECEIVE provider={} method={} url={} status={} elapsedMs={} headerCount={} body={}",
                    config.provider(),
                    requestSpec.method(),
                    requestSpec.uri(),
                    response.getStatusCode().value(),
                    elapsedMs,
                    response.getHeaders() != null ? response.getHeaders().size() : 0,
                    summarizeBody(response.getBody())
            );
            return response;
        } catch (RuntimeException e) {
            long elapsedMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            log.warn(
                    "REST ERROR provider={} method={} url={} elapsedMs={} message={}",
                    config.provider(),
                    requestSpec.method(),
                    requestSpec.uri(),
                    elapsedMs,
                    e.getMessage()
            );
            throw e;
        }
    }

    private Map<String, String> resolveHeaders(RestProviderResolvedConfig config, RestProviderRequestSpec requestSpec) {
        Map<String, String> headers = new LinkedHashMap<>();
        Map<String, String> sourceHeaders = requestSpec.headers();
        if (sourceHeaders != null && !sourceHeaders.isEmpty()) {
            headers.putAll(sourceHeaders);
        }
        return headers;
    }

    private RestClient createClient(RestProviderResolvedConfig config) {
        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.connectTimeoutMs()))
                .followRedirects(toHttpRedirect(config.followRedirects()));

        if (config.virtualThreadsEnabled()) {
            ExecutorService executorService = virtualThreadExecutorProvider.getIfAvailable();
            if (executorService != null) {
                httpClientBuilder.executor(executorService);
            }
        }

        applyProxy(config, httpClientBuilder);
        applyInsecureSsl(config, httpClientBuilder);

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClientBuilder.build());
        requestFactory.setReadTimeout(Duration.ofMillis(config.responseTimeoutMs()));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    private void copyHeaders(Map<String, String> source, HttpHeaders target) {
        if (source == null || source.isEmpty()) {
            return;
        }
        source.forEach((key, value) -> {
            if (key == null || key.isBlank() || value == null) {
                return;
            }
            target.add(key, value);
        });
    }

    private String summarizeBody(Object body) {
        if (body == null) {
            return "none";
        }
        if (body instanceof String text) {
            return "string(len=" + text.length() + ")";
        }
        if (body instanceof Map<?, ?> map) {
            return "object(keys=" + map.size() + ")";
        }
        if (body instanceof Collection<?> collection) {
            return "array(size=" + collection.size() + ")";
        }
        if (body.getClass().isArray()) {
            return "array(size=" + java.lang.reflect.Array.getLength(body) + ")";
        }
        return body.getClass().getSimpleName();
    }

    private HttpClient.Redirect toHttpRedirect(RestProviderResolvedConfig.HttpRedirect redirect) {
        return switch (redirect) {
            case ALWAYS -> HttpClient.Redirect.ALWAYS;
            case NEVER -> HttpClient.Redirect.NEVER;
            default -> HttpClient.Redirect.NORMAL;
        };
    }

    private void applyProxy(RestProviderResolvedConfig config, HttpClient.Builder httpClientBuilder) {
        RestProviderResolvedConfig.Proxy proxy = config.proxy();
        if (proxy == null || proxy.host() == null || proxy.host().isBlank() || proxy.port() == null || proxy.port() < 1) {
            return;
        }
        httpClientBuilder.proxy(ProxySelector.of(new InetSocketAddress(proxy.host(), proxy.port())));
    }

    private void applyInsecureSsl(RestProviderResolvedConfig config, HttpClient.Builder httpClientBuilder) {
        if (!config.insecureSsl()) {
            return;
        }
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new InsecureTrustManager()}, new SecureRandom());
            SSLParameters sslParameters = new SSLParameters();
            sslParameters.setEndpointIdentificationAlgorithm("");

            httpClientBuilder.sslContext(sslContext);
            httpClientBuilder.sslParameters(sslParameters);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to configure insecure SSL for REST provider", e);
        }
    }

    private record ClientKey(
            String provider,
            int connectTimeoutMs,
            int responseTimeoutMs,
            boolean virtualThreadsEnabled,
            boolean insecureSsl,
            RestProviderResolvedConfig.HttpRedirect followRedirects,
            String proxyHost,
            Integer proxyPort
    ) {
        static ClientKey from(RestProviderResolvedConfig config) {
            RestProviderResolvedConfig.Proxy proxy = config.proxy();
            return new ClientKey(
                    config.provider(),
                    config.connectTimeoutMs(),
                    config.responseTimeoutMs(),
                    config.virtualThreadsEnabled(),
                    config.insecureSsl(),
                    config.followRedirects(),
                    proxy != null ? proxy.host() : null,
                    proxy != null ? proxy.port() : null
            );
        }
    }

    private static class InsecureTrustManager implements X509TrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }
    }
}

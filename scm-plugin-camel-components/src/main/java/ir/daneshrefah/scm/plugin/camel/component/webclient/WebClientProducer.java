package ir.daneshrefah.scm.plugin.camel.component.webclient;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.ProxyProvider;
import reactor.util.retry.Retry;

import javax.net.ssl.SSLException;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

public class WebClientProducer extends DefaultProducer {

    private final WebClient client;

    public WebClientProducer(WebClientEndpoint endpoint) {
        super(endpoint);

        ConnectionProvider provider = ConnectionProvider.builder("webclient-%s-pool".formatted(endpoint.getEndpointKey()))
                .maxConnections(endpoint.getMaxConnections())
                .pendingAcquireTimeout(Duration.ofSeconds(endpoint.getAcquireTimeoutSeconds()))
                .maxIdleTime(Duration.ofMillis(endpoint.getMaxIdleTime()))
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, endpoint.getConnectTimeout())
                .doOnConnected(conn ->
                        conn.addHandlerLast(new io.netty.handler.timeout.WriteTimeoutHandler(endpoint.getWriteTimeout(),
                                java.util.concurrent.TimeUnit.MILLISECONDS))
                )
                .responseTimeout(Duration.ofMillis(endpoint.getResponseTimeout()))
                .compress(endpoint.isCompress());

        if (endpoint.getProxyHost() != null && endpoint.getProxyPort() != null) {
            httpClient = httpClient.proxy(proxy -> proxy
                    .type(ProxyProvider.Proxy.HTTP)
                    .host(endpoint.getProxyHost())
                    .port(endpoint.getProxyPort()));
        }

        //TODO SCMNEW-5: handel with scm exception
        if (endpoint.isInsecureSsl()) {
            try {
                httpClient = httpClient.secure(ssl -> {
                    try {
                        ssl.sslContext(SslContextBuilder.forClient()
                                .trustManager(InsecureTrustManagerFactory.INSTANCE).build());
                    } catch (SSLException e) {
                        throw new RuntimeException("Failed to configure insecure SSL", e);
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException("Failed to enable insecure SSL", e);
            }
        }


        if (endpoint.isWiretap()) {
            httpClient = httpClient.wiretap(true);
        }

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(endpoint.getMaxInMemorySize()))
                .build();


        WebClient.Builder builder = WebClient.builder()
                .clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies);
        if (endpoint.getProxyUsername() != null && endpoint.getProxyPassword() != null) {
            String creds = endpoint.getProxyUsername() + ":" + endpoint.getProxyPassword();
            String encoded = Base64.getEncoder().encodeToString(creds.getBytes());
            builder.defaultHeader("Proxy-Authorization", "Basic " + encoded);
        }

        this.client = builder.build();
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        WebClientEndpoint endpoint = getEndpoint();
        String uri = endpoint.getRawUri();
        String method = endpoint.getMethod().toUpperCase();
        String body = exchange.getIn().getBody(String.class);
        Map<String, Object> headers = exchange.getIn().getHeaders();

        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        WebClient.RequestBodySpec requestSpec = client.method(httpMethod).uri(uri);


        headers
                .entrySet().stream()
                .filter(entry -> {
                    if (StringUtils.containsIgnoreCase(entry.getKey(), "camelHttp")) {
                        return false;
                    }
                    if (StringUtils.containsIgnoreCase(entry.getKey(), "camelServlet")) {
                        return false;
                    }
                    if (StringUtils.equalsIgnoreCase(entry.getKey(), "host")) {
                        return false;
                    }
                    return entry.getValue() != null;
                })
                .forEach(entry -> requestSpec.header(entry.getKey(), Objects.requireNonNull(entry.getValue()).toString()));

        Mono<String> response = (Objects.equals(HttpMethod.GET, httpMethod) || Objects.equals(HttpMethod.DELETE, httpMethod))
                ? requestSpec.retrieve().bodyToMono(String.class)
                : requestSpec.bodyValue(body).retrieve().bodyToMono(String.class);

        if (endpoint.isRetryEnabled()) {
            response = response.retryWhen(
                    Retry.backoff(endpoint.getMaxAttempts(),
                            Duration.ofMillis(endpoint.getMinBackoff())));
        }

        String result = response.block();
        exchange.getMessage().setBody(result);
    }

    @Override
    public WebClientEndpoint getEndpoint() {
        return (WebClientEndpoint) super.getEndpoint();
    }
}

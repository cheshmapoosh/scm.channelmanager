package ir.daneshrefah.scm.plugin.camel.component.webclient;

import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class WebClientProducer extends DefaultProducer {

    private final RestClient restClient;

    public WebClientProducer(WebClientEndpoint endpoint) {
        super(endpoint);


        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(endpoint.getConnectTimeout()));


        if (endpoint.getProxyHost() != null && endpoint.getProxyPort() != null) {
            httpClientBuilder.proxy(ProxySelector.of(
                    new InetSocketAddress(endpoint.getProxyHost(), endpoint.getProxyPort())
            ));
        }


        if (endpoint.isInsecureSsl()) {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{new InsecureTrustManager()}, new SecureRandom());
                httpClientBuilder.sslContext(sslContext);
            } catch (Exception e) {
                throw new RuntimeException("Failed to configure insecure SSL", e);
            }
        }


        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClientBuilder.build());

        RestClient.Builder builder = RestClient.builder()
                .requestFactory(factory);


        if (endpoint.getProxyUsername() != null && endpoint.getProxyPassword() != null) {
            String creds = endpoint.getProxyUsername() + ":" + endpoint.getProxyPassword();
            String encoded = Base64.getEncoder().encodeToString(creds.getBytes());
            builder.defaultHeader("Proxy-Authorization", "Basic " + encoded);
        }

        this.restClient = builder.build();
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        WebClientEndpoint endpoint = getEndpoint();
        String uri = endpoint.getRawUri();
        String method = endpoint.getMethod().toUpperCase();
        String body = exchange.getIn().getBody(String.class);
        Map<String, Object> headers = exchange.getIn().getHeaders();

        //TODO Open Banking have encoding GZIP and send response by UNICODE
        exchange.getMessage().getHeaders().put("Accept-Encoding", "identity");

        HttpMethod httpMethod = HttpMethod.valueOf(method);

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        ResponseEntity<String> entity = new ResponseEntity<>(
                HttpStatus.ACCEPTED);
        if (Objects.equals(HttpMethod.DELETE, httpMethod)) {
            entity = restClient.delete()
                    .uri(uri)
                    .headers(getHttpHeadersConsumer(headers))
                    .retrieve()
                    .toEntity(String.class);
        } else if (Objects.equals(HttpMethod.GET, httpMethod)) {
            entity = restClient.get()
                    .uri(uri)
                    .headers(getHttpHeadersConsumer(headers))
                    .retrieve()
                    .toEntity(String.class);
        } else {
            entity = restClient.post()
                    .uri(uri)
                    .body(body)
                    .contentLength(bytes.length)
                    .headers(getHttpHeadersConsumer(headers))
                    .retrieve()
                    .toEntity(String.class);
        }

        String result = entity.getBody();
        exchange.getMessage().setBody(result);
    }

    private Consumer<HttpHeaders> getHttpHeadersConsumer(Map<String, Object> headers) {
        return httpHeaders -> {
            headers.entrySet().stream()
                    .filter(entry -> {
                        String key = entry.getKey();

                        if (StringUtils.containsIgnoreCase(key, "camelHttp")) return false;
                        if (StringUtils.containsIgnoreCase(key, "camelServlet")) return false;
                        if (StringUtils.equalsIgnoreCase(key, "host")) return false;


                        return entry.getValue() != null;
                    })
                    .forEach(entry -> httpHeaders.add(entry.getKey(), entry.getValue().toString()));
        };
    }


    @Override
    public WebClientEndpoint getEndpoint() {
        return (WebClientEndpoint) super.getEndpoint();
    }

    private static class InsecureTrustManager implements X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
    }
}
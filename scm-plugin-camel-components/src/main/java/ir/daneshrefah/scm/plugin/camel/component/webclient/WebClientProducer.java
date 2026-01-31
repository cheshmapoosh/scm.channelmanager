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
        exchange.getMessage().getHeaders().put("Accept-Encoding", "identity");
        exchange.getMessage().getHeaders().put("Authorization", "Bearer " +
                "eyJhbGciOiJSUzI1NiIsImtpZCI6IjZCN0FDQzUyMDMwNUJGREI0RjcyNTJEQUVCMjE3N0NDMDkxRkFBRTFSUzI1NiIsInR5cCI6ImF0K2p3dCIsIng1dCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSJ9.eyJuYmYiOjE3Njk4Mzg4NDcsImV4cCI6MTc2OTg0NjA0NywiaXNzIjoibnVsbCIsImNsaWVudF9pZCI6ImNtX2RldmVsb3AiLCJpYXQiOjE3Njk4Mzg4NDcsInNjb3BlIjpbImNxOmFjdDpwb3N0IiwiY3E6YWN0LXN0YXR1czpwb3N0IiwiY3E6Y2FydGFibGU6cG9zdCIsImNxOmNoZXF1ZS1jbHI6cG9zdCIsImNxOmNoZXF1ZS1jbHItZHQ6cG9zdCIsImNxOmNoZXF1ZS1jbHItaW5xOnBvc3QiLCJjcTpjaGVxdWUtY2xyLWxzdDpwb3N0IiwiY3E6Y2hlcXVlLWNsci1zdGF0dXM6cG9zdCIsImNxOmNoZXF1ZS1pbmZvOnBvc3QiLCJjcTpjaGVxdWUtaXNzdWVkLXN5ZDpwb3N0IiwiY3E6Y2hlcXVlLWxzdDpwb3N0IiwiY3E6Y2hlcXVlLXJxc3Q6cG9zdCIsImNxOmNoZXF1ZS1zYXlhZC1saXN0OnBvc3QiLCJjcTpjaGVxdWUtc3lkOnBvc3QiLCJjcTpjaGVxdWUtd2FpdC1zeWQ6cG9zdCIsImNxOmNobG5nLWNvZGU6cG9zdCIsImNxOmRlYWN0OnBvc3QiLCJjcTppbnEtc3RhdHVzLWJ0Y2g6cG9zdCIsImNxOmlucXVpcmUtYWN0aXZlLWNoZXF1ZS1sc3Q6cG9zdCIsImNxOmlucXVpcmUtY2hlcXVlLXJxc3Q6cG9zdCIsImNxOmlucXVpcnktc3RhdHVzOnBvc3QiLCJjcTppc3N1ZTpwb3N0IiwiY3E6cmV2b2tlOnBvc3QiLCJmdzpnZXQtYmlsbC1kZWJ0IiwiZnc6Z2V0LWJpbGwtcmV2b2tlIiwiZnc6Z2V0LWRldGFpbC1UcmFuc0lkIiwiZnc6cGF5LWJpbGwtZGVidCIsImdiOmNhci1maW5lcyIsImdiOnJlcG9ydC1iaWxsLXBheW1lbnQiLCJnYjp0cmFmZmljLWltZyIsImluczpkZXBvc2l0LWlkLWlucXVpcnk6cG9zdCIsInBrOmNoZXF1ZS1hY2NlcHQiLCJwazpjaGVxdWUtaW5xdWlyeTpwb3N0IiwicGs6Y2hlcXVlLXRyYW5zZmVyIiwicGs6Y3VzdG9tZXItaW5xdWlyeTpwb3N0IiwicGs6Z2l2ZS1iYWNrIiwicGs6aXNzdWVyLWlucXVpcnkiLCJwazpyZWNlaXZlci1pbnF1aXJ5OnBvc3QiLCJwdTp6ZW1hbmF0bmFtZWg6cG9zdCIsInJiOmJpbGwtYWJmYTpnZXQiLCJyYjpiaWxsLWdhczpnZXQiLCJyYjpiaWxsLW1vYmlsZTpnZXQiLCJyYjpiaWxsLXBob25lOmdldCIsInJiOmJpbGwtdGF2YW5pcjpnZXQiLCJzaWduLWV4aXN0YW5jZSIsInN0cDp1c2VyLWJ1eTpwb3N0Iiwic3RwOnVzZXItcGF5bWVudDpwb3N0Il19.RE628lutddJ2-sOviB6_7kl19ejzXMeS8C0It0onA0FZpyLi10wFJ02e_RcelNv5IBHmSrd5yD4MX0UwVGVHIIo4mJjfOrbUe63n3i46gF8F6I4jWnuJrcuifOSmJJxcn5YY7EDdnNrs3U_3mJsyXul4us3TbAO4lH3fczcxsKmuRKtp4iN86Fbc_kFA1br0dKaJ-ZrD1htrtmA-rNxdDURawhuJVBO_yom_gpESkrEZs9Uj5VJ_z0OmgCLEKRVCZCUda3v75IIp0BcpBxWCdYf9xO6-Vvg3UP3YV5VQGEr9jzpjRGgktB7QhZXVIasD5iiiEMuWMAionGhPKdNe3A");

        HttpMethod httpMethod = HttpMethod.valueOf(method);

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        ResponseEntity<String> entity =new ResponseEntity<>(
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
            try {
                entity = restClient.post()
                        .uri(uri)
                        .body(body)
                        .contentLength(bytes.length)
                        .headers(getHttpHeadersConsumer(headers))
                        .retrieve()
                        .toEntity(String.class);
            }catch (Exception e) {}
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
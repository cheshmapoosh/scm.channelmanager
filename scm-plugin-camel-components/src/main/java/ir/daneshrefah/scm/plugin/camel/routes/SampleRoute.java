package ir.daneshrefah.scm.plugin.camel.routes;

import io.netty.handler.codec.http.HttpHeaderValues;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.Headers;
import org.apache.camel.builder.RouteBuilder;
import org.apache.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class SampleRoute extends RouteBuilder {
    @Override
    public void configure() {
        from("timer:trigger?period=30000")
                .process(exchange -> {
                    exchange.getIn().setBody("Hello World");
                })
                .setHeader("targetUrl", constant("https://httpbin.org/post"))
                .setHeader(HttpHeaders.CONTENT_TYPE, constant(HttpHeaderValues.APPLICATION_JSON))
                .setProperty("httpMethod").exchange(exchange ->  {
                    System.out.printf("[%s] %s\n", Thread.currentThread().getName(), exchange.getIn().getBody(String.class));
                    return "POST";
                })
                .toD("webclient:${header.targetUrl}" +
                        "?method=${exchangeProperty.httpMethod}" +
                        "&responseTimeout=1000" +
                        "&connectTimeout=50" +
                        "&writeTimeout=100" +
                        "&retryEnabled=true" +
                        "&maxAttempts=3" +
                        "&minBackoff=1000" +
                        "&wiretap=true")
                .log("WebClient response: ${body}");
    }
}

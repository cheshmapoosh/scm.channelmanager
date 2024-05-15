package ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest.swagger;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-28
 */
@Component
public class SwaggerUIGenerator {

    @Value("${server.port}")
    private Integer serverPort;
    @Value("${server.servlet.context-path}")
    private String servletContextPrefix;
    @Value("${scm.swagger.server-host:#{null}}")
    private String serverHost;
    private static SwaggerUIGenerator SWAGGER_UI_HANDLER;

    @PostConstruct
    public void init() {
        if (null != SWAGGER_UI_HANDLER) {
            throw new RuntimeException("multiple instance of SwaggerUIGenerator is created!" );
        }
        SWAGGER_UI_HANDLER = this;
    }

    public static SwaggerUIGenerator getInstance() {
        return SWAGGER_UI_HANDLER;
    }

    private String generateServerAddress() {
        final String ipAddress = "{ip-address}";
        String baseUrl = "http://" + ipAddress;
        try {
            String host = Objects.nonNull(serverHost) ? serverHost : InetAddress.getLocalHost().getHostAddress();
            serverHost = host;
            return baseUrl.replace(ipAddress, host);
        } catch (Exception e) {
            return baseUrl.replace(ipAddress, "0.0.0.0");
        }
    }

    public String generateCamelUIBody(Integer port, String contextPath, String defaultSwaggerJsonPath)  {
        String targetUrl = "{server-address}:{provider-port}{provider-context-path}{swagger-home-address}";
        String script = """
                <script>
                 window.location.replace('{server-address}:{server-port}{servlet-context-prefix}/swagger/index.html?url={target-swagger-json-server}');
                </script>
                """;
        targetUrl = targetUrl.replace("{server-address}", getInstance().generateServerAddress());
        targetUrl = targetUrl.replace("{provider-port}",port.toString());
        targetUrl = targetUrl.replace("{provider-context-path}",contextPath);
        targetUrl = targetUrl.replace("{swagger-home-address}",defaultSwaggerJsonPath);
        script = script.replace("{server-address}", getInstance().generateServerAddress());
        script = script.replace("{server-port}",serverPort.toString());
        script = script.replace("{servlet-context-prefix}",servletContextPrefix);
        script = script.replace("{target-swagger-json-server}", URLEncoder.encode(targetUrl, StandardCharsets.UTF_8));
        return script;
    }


}

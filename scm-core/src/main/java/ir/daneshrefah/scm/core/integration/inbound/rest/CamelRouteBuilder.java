package ir.daneshrefah.scm.core.integration.inbound.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import ir.daneshrefah.scm.common.model.message.*;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.ServiceType;
import ir.daneshrefah.scm.core.config.ApplicationConfig;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.rest.RestBindingMode;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.function.BiFunction;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public class CamelRouteBuilder extends RouteBuilder {

    private static final String JSON_PROPERTY_METADATA_PORT = "port";
    private static final String JSON_PROPERTY_METADATA_CONTEXT_PATH = "contextPath";
    private Channel channel;
    private BiFunction<TerminalServiceChannelAccess, Message, Message> serviceInvoker;
    private JacksonDataFormat dataFormat;

    private RestMessageParser restMessageParser;
    private RestResponseGenerator restResponseGenerator;

    public CamelRouteBuilder(Channel channel, BiFunction<TerminalServiceChannelAccess, Message, Message> serviceInvoker) {
        this.channel = channel;
        this.serviceInvoker = serviceInvoker;

        ObjectMapper objectMapper = ApplicationConfig.getObjectMapperInstance();
//        objectMapper.registerModule(new JavaTimeModule());
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Message.class, new MessageRestSerializer());
//        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(simpleModule);
        dataFormat = new JacksonDataFormat();
        dataFormat.setObjectMapper(objectMapper);
        restMessageParser = new RestMessageParser();
        restResponseGenerator = new RestResponseGenerator();
    }


    @Override
    public void configure() throws Exception {
        JsonNode metadataJson = new ObjectMapper().readTree(channel.getMetadata());
        Integer port = metadataJson.get(JSON_PROPERTY_METADATA_PORT).intValue();
        String contextPath = metadataJson.get(JSON_PROPERTY_METADATA_CONTEXT_PATH).textValue();
        restConfiguration().host("0.0.0.0").port(port).bindingMode(RestBindingMode.json)
                .enableCORS(true) // <-- Important
                .corsAllowCredentials(true) // <-- Important
                .corsHeaderProperty("Access-Control-Allow-Origin","*")
                .corsHeaderProperty("Access-Control-Allow-Headers","Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization")
                .contextPath(contextPath);

    }

    private String extractServiceUrl(Service service) {
        if (null == service) {
            return null;
        }
        String serviceUrl = StringUtils.isNotEmpty(service.getAlias()) ? service.getAlias() : service.getCode();
        if (!StringUtils.startsWith(serviceUrl, "/", true)) {
            serviceUrl = "/" + serviceUrl;
        }
        return serviceUrl.toLowerCase().replace("_", "-");
    }
    public void addRoute(TerminalServiceChannelAccess channelAccess) {
        String terminalCode = channelAccess.getTerminalServiceAccess().getTerminal().getCode();
        String httpMethod = createHttpMethodBasedOnServiceType(channelAccess.getTerminalServiceAccess().getService().getType());
        String serviceUrl = extractServiceUrl(channelAccess.getTerminalServiceAccess().getService());
        String parentServiceUrl = extractServiceUrl(channelAccess.getTerminalServiceAccess().getService().getParent());
        StringBuilder urlBuilder = new StringBuilder("rest:");
        urlBuilder
                .append(httpMethod)
                .append(":api/")
                .append(terminalCode)
                .append(null != parentServiceUrl ? parentServiceUrl : "")
                .append(serviceUrl);
        String inboundUrl = urlBuilder.toString();
        from(inboundUrl)
                .process(exchange -> {
                    // init message
                    Message message = restMessageParser.extractBody(exchange, channelAccess);
                    exchange.getMessage().setBody(message, Message.class);
                })
                .process(exchange -> {
                    // invoke service
                    Message message = exchange.getMessage().getBody(Message.class);
                    message = serviceInvoker.apply(channelAccess, message);
                })
                .process(exchange -> {
                    Message message = exchange.getMessage().getBody(Message.class);
                    message.addEvent(EventType.WHOLE, message.getHeader().getReceiveTimestamp(),
                            LocalDateTime.now(), true, null, null, null);
                    restResponseGenerator.initResponseHeader(exchange);
                })
                .marshal(dataFormat)
                .end();

    }

    private String createHttpMethodBasedOnServiceType(ServiceType type) {
        String defaultMethod = "post";
        if (null == type) {
            return defaultMethod;
        }
        switch (type) {
            case INQUIRY:
                return "get";
            case REPORT:
                return "get";
            case FINANCE:
                return "post";
            default:
                return defaultMethod;
        }
    }
}

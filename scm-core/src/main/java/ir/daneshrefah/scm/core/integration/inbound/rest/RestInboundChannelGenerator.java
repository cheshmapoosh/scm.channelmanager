package ir.daneshrefah.scm.core.integration.inbound.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ir.daneshrefah.scm.plugin.api.inbound.AbstractInboundChannelGenerator;
import ir.daneshrefah.scm.plugin.api.model.message.EventType;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.terminal.Channel;
import ir.daneshrefah.scm.plugin.api.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.plugin.api.service.ServiceProducerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */
public class RestInboundChannelGenerator extends AbstractInboundChannelGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestInboundChannelGenerator.class);

    private static final String JSON_PROPERTY_METADATA_PORT = "port";
    private static final String JSON_PROPERTY_METADATA_CONTEXT_PATH = "contextPath";

    public RestInboundChannelGenerator(ServiceProducerTemplate producerTemplate, Channel channel) {
        super(producerTemplate, channel);
    }

    @Override
    public void configure() throws Exception {
        JsonNode metadataJson = new ObjectMapper().readTree(channel.getMetadata());
        Integer port = metadataJson.get(JSON_PROPERTY_METADATA_PORT).intValue();
        String contextPath = metadataJson.get(JSON_PROPERTY_METADATA_CONTEXT_PATH).textValue();
        restConfiguration().host("localhost").port(port).bindingMode(RestBindingMode.json);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Message.class, new MessageRestSerializer());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(simpleModule);
        JacksonDataFormat dataFormat = new JacksonDataFormat();
        dataFormat.setObjectMapper(objectMapper);


        LOGGER.info("*********** start define rest inbound services. ***********");

        String inboundUrl = null;
        for (Iterator<TerminalServiceChannelAccess> iterator = channelAccesses.iterator(); iterator.hasNext(); ) {
            TerminalServiceChannelAccess channelAccess = iterator.next();
            String serviceCode = channelAccess.getTerminalServiceAccess().getService().getCode();
            String terminalCode = channelAccess.getTerminalServiceAccess().getTerminal().getCode();
            inboundUrl = "rest:post:api" + contextPath + "/" + terminalCode + "/" + serviceCode;
            from(inboundUrl)
                    .doTry()
                    .process(exchange -> {
                        new RestMessageInitializer().initMessageBody(exchange, channelAccess);
                    })
                    .log("body ${body}")
                    .process(exchange -> {
                        producerTemplate.executeService(serviceCode, exchange.getMessage().getBody(Message.class));
                    })
//                    .to("direct:SVI_" + serviceCode)
                    .process(exchange -> {
                        System.out.println("nowi");
                    })
                    .doCatch(Exception.class)
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
//                            exchange.getMessage().setBody("error");
                        }
                    })
                    .end()
                    .process(exchange -> {
                        Message message = exchange.getMessage().getBody(Message.class);
                        message.addEvent(EventType.WHOLE, message.getHeader().getReceiveTimestamp(),
                                LocalDateTime.now(), true, null);
                        new RestResponseInitializer().initResponseHeader(exchange);
                    })
                    .marshal(dataFormat)
                    .end();
            LOGGER.info("rest inbound '{}' related to service '{}' registered.", inboundUrl, serviceCode);
        }

        LOGGER.info("*********** end define rest inbound services. ***********");

    }

    public static String getProtocolKey() {
        return "REST";
    }
}

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
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.rest.RestBindingMode;

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

    private static final String JSON_PROPERTY_METADATA_PORT = "port";
    private static final String JSON_PROPERTY_METADATA_CONTEXT_PATH = "contextPath";

    public RestInboundChannelGenerator(Channel channel) {
        super(channel);
    }

    @Override
    public void configure() throws Exception {
        JsonNode metadataJson = new ObjectMapper().readTree(channel.getMetadata());
        Integer port = metadataJson.get(JSON_PROPERTY_METADATA_PORT).intValue();
        String contextPath = metadataJson.get(JSON_PROPERTY_METADATA_CONTEXT_PATH).textValue();
        restConfiguration().host("localhost").port(port).bindingMode(RestBindingMode.json);
//        onException(Exception.class)
//                .handled(true)
//                .transform().constant("Sorry");

        // this is just the generic error handler where we set the
        // destination
        // and the number of redeliveries we want to try
//        errorHandler(deadLetterChannel("mock:error").maximumRedeliveries(1));


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Message.class, new MessageRestSerializer());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(simpleModule);
        JacksonDataFormat dataFormat = new JacksonDataFormat();
        dataFormat.setObjectMapper(objectMapper);


        for (Iterator<TerminalServiceChannelAccess> iterator = channelAccesses.iterator(); iterator.hasNext(); ) {
            TerminalServiceChannelAccess channelAccess = iterator.next();
            String serviceCode = channelAccess.getTerminalServiceAccess().getService().getCode();
            String terminalCode = channelAccess.getTerminalServiceAccess().getTerminal().getCode();
            from("rest:post:api" + contextPath + "/" + terminalCode + "/" + serviceCode)
                    .doTry()
                    .process(exchange -> {
                        new RestMessageInitializer().initMessageBody(exchange, channelAccess);
//                        exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class).getMessage()
                    })
                    .log("body ${body}")
                    .to("direct:SVI_" + serviceCode)
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
                        message.addEvent(EventType.WHOLE, message.getHeader().getReceiveTimestamp(), LocalDateTime.now(), "");
                        new RestResponseInitializer().initResponseHeader(exchange);
                    })
                    .marshal(dataFormat)
                    .end();
        }
        /*for (Iterator<TerminalServiceChannelAccess> iterator = channelAccesses.iterator(); iterator.hasNext(); ) {
            TerminalServiceChannelAccess channelAccess = iterator.next();
            String serviceCode = channelAccess.getTerminalServiceAccess().getService().getCode();
            String terminalCode = channelAccess.getTerminalServiceAccess().getTerminal().getCode();
            from("rest:post:api" + restChannel.getContext() + "/" + terminalCode + "/" + serviceCode)
                    .log("body ${body}")
                    .process(exchange -> {
                        JsonNode requestBody = exchange.getMessage().getBody(JsonNode.class);
                        Message message = new Message();
                        Header header = new Header();
                        header.setService(channelAccess);
                        header.setCorrelationId(RandomStringUtils.randomAlphanumeric(10));
                        message.setHeader(header);
                        message.setPayload(requestBody);
                        exchange.getMessage().setBody(message, Message.class);

                        String requestJsonSchema = channelAccess.getTerminalServiceAccess().getService().getRequestJSONSchema();

                        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
                        JsonSchema schema = factory.getSchema(requestJsonSchema);

                        Set<ValidationMessage> errors = schema.validate(requestBody);
                        if (errors.size() > 0) {
                            List<Error> errorList = new ArrayList<>();
                            for (Iterator<ValidationMessage> errorIterator = errors.iterator(); errorIterator.hasNext(); ) {
                                ValidationMessage validationMessage = errorIterator.next();
                                Error error = new Error(validationMessage.getCode(), validationMessage.getMessage(), "JSONSchema");
                                errorList.add(error);
                            }
                            throw new ValidationException(message, errorList);
                        }
                        System.out.println("now validate");
                    })
                    .process(exchange -> {
                        System.out.println(exchange.getMessage().getBody(Message.class).getHeader().getCorrelationId());
                    })
                    .log("inbound body is : ${body}")
                    .to("direct:SERVICE_" + serviceCode)
                    .onException(Exception.class)
                    .handled(true)
                    .process(exchange -> {
                        BaseException exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, BaseException.class);
                        Message message = exception.getIncomeMessage();
                        message.setErrors(exception.getErrors());
                        exchange.getMessage().setBody(message);
                    }).marshal().json()
                    .end();
        }*/

//        from("direct:test")
//                .setBody().constant("Helloooooo2")
//                .end();
    }

    public static String getProtocolKey() {
        return "REST";
    }
}

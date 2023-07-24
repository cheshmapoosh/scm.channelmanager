package ir.daneshrefah.scm.core.integration.inbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import ir.daneshrefah.scm.plugin.api.inbound.AbstractInboundChannelGenerator;
import ir.daneshrefah.scm.plugin.api.model.message.Header;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.terminal.Channel;
import ir.daneshrefah.scm.plugin.api.model.terminal.TerminalServiceChannelAccess;
import org.apache.camel.Exchange;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
        for (Iterator<TerminalServiceChannelAccess> iterator = channelAccesses.iterator(); iterator.hasNext(); ) {
            TerminalServiceChannelAccess channelAccess = iterator.next();
            String serviceCode = channelAccess.getTerminalServiceAccess().getService().getCode();
            String terminalCode = channelAccess.getTerminalServiceAccess().getTerminal().getCode();
            from("rest:post:api" + contextPath + "/" + terminalCode + "/" + serviceCode)
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

                    })
                    .to("direct:SVI_" + serviceCode)
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

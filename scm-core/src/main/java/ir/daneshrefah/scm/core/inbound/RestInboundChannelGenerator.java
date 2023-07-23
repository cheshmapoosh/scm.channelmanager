package ir.daneshrefah.scm.core.inbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import ir.daneshrefah.scm.common.model.message.Header;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.RestChannel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import org.apache.camel.Exchange;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

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


    public RestInboundChannelGenerator(Channel channel, List<TerminalServiceChannelAccess> channelAccesses) {
        super(channel, channelAccesses);
    }

    @Override
    public void configure() throws Exception {
        RestChannel restChannel = (RestChannel) channel;
        restConfiguration().host("localhost").port(restChannel.getPort()).bindingMode(RestBindingMode.json);
        for (Iterator<TerminalServiceChannelAccess> iterator = channelAccesses.iterator(); iterator.hasNext(); ) {
            TerminalServiceChannelAccess channelAccess = iterator.next();
            String serviceCode = channelAccess.getTerminalServiceAccess().getService().getCode();
            String terminalCode = channelAccess.getTerminalServiceAccess().getTerminal().getCode();
            from("rest:post:api" + restChannel.getContext() + "/" + terminalCode + "/" + serviceCode)
                    .log("body ${body}")
                    .process(exchange -> {
                        JsonNode requestBody = exchange.getMessage().getBody(JsonNode.class);
                        String requestJsonSchema = channelAccess.getTerminalServiceAccess().getService().getRequestJSONSchema();

                        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
                        com.networknt.schema.JsonSchema schema = factory.getSchema(requestJsonSchema);

                        Set<ValidationMessage> errors = schema.validate(requestBody);
                        if (errors.size() > 0) {
                            throw new RuntimeException("invalid input json");
                        }
                        System.out.println("now validate");
                    })
                    .process(exchange -> {
                        String oldBody = exchange.getMessage().getBody(String.class);
                        Message message = new Message();
                        Header header = new Header();
                        header.setService(channelAccess);
                        header.setCorrelationId(RandomStringUtils.randomAlphanumeric(10));
                        message.setHeader(header);
                        message.setPayload(oldBody);
                        exchange.getMessage().setBody(message, Message.class);
                        System.out.println(exchange.getMessage().getBody(Message.class).getHeader().getCorrelationId());
                    })
                    .log("inbound body is : ${body}")
                    .to("direct:SERVICE_" + serviceCode)
                    .onException(Exception.class)
                    .handled(true)
                    .process(exchange -> {
                        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                        exchange.getMessage().setBody(exception.getMessage());
                    })
                    .end();
        }

//        from("direct:test")
//                .setBody().constant("Helloooooo2")
//                .end();
    }

}

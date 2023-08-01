package ir.daneshrefah.scm.plugin.nab.inbound;

import ir.daneshrefah.scm.plugin.api.inbound.AbstractInboundChannelGenerator;
import ir.daneshrefah.scm.plugin.api.model.terminal.Channel;
import ir.daneshrefah.scm.plugin.api.model.terminal.TerminalServiceChannelAccess;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */
public class NabCustomInboundChannelGenerator extends AbstractInboundChannelGenerator {


    public NabCustomInboundChannelGenerator(Channel channel) {
        super(channel);
    }

    @Override
    public void configure() throws Exception {
        /*RestChannel restChannel = (RestChannel) channel;
        restConfiguration().host("localhost").port(restChannel.getPort()).bindingMode(RestBindingMode.json);
        for (Iterator<TerminalServiceChannelAccess> iterator = channelAccesses.iterator(); iterator.hasNext(); ) {
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
        return "NAB";
    }
}

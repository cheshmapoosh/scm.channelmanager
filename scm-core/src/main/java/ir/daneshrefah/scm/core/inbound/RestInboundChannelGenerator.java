package ir.daneshrefah.scm.core.inbound;

import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.RestChannel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

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
                    .to("direct:SERVICE_" + serviceCode)
                    .end();
        }

//        from("direct:test")
//                .setBody().constant("Helloooooo2")
//                .end();
    }

}

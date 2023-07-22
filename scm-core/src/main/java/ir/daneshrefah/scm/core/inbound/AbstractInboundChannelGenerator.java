package ir.daneshrefah.scm.core.inbound;

import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */
public abstract class AbstractInboundChannelGenerator extends RouteBuilder {

    protected Channel channel;
    protected List<TerminalServiceChannelAccess> channelAccesses;

    public AbstractInboundChannelGenerator(Channel channel, List<TerminalServiceChannelAccess> channelAccesses) {
        this.channel = channel;
        this.channelAccesses = channelAccesses;
    }

}

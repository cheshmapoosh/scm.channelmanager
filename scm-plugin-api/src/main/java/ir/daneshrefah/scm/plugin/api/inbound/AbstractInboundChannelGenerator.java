package ir.daneshrefah.scm.plugin.api.inbound;

import ir.daneshrefah.scm.plugin.api.model.terminal.Channel;
import ir.daneshrefah.scm.plugin.api.model.terminal.TerminalServiceChannelAccess;
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

    public AbstractInboundChannelGenerator(Channel channel) {
        this.channel = channel;
    }

    public void setChannelAccesses(List<TerminalServiceChannelAccess> channelAccesses) {
        this.channelAccesses = channelAccesses;
    }

    public static String getProtocolKey() {
        return null;
    }

}

package ir.daneshrefah.scm.plugin.api.inbound;

import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.terminal.Channel;
import ir.daneshrefah.scm.plugin.api.model.terminal.TerminalServiceChannelAccess;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;

import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */
public abstract class AbstractInboundChannelGenerator {

    protected Channel channel;
    protected List<TerminalServiceChannelAccess> channelAccesses;
    protected ServiceProducerTemplate producerTemplate;
    protected ApplicationContext applicationContext;
    public AbstractInboundChannelGenerator(ApplicationContext applicationContext, ServiceProducerTemplate producerTemplate, Channel channel) {
        this.producerTemplate = producerTemplate;
        this.channel = channel;
        this.applicationContext = applicationContext;
    }

    public void setChannelAccesses(List<TerminalServiceChannelAccess> channelAccesses) {
        this.channelAccesses = channelAccesses;
    }

    public final void initInbound() {
        initConfig();
        for (Iterator<TerminalServiceChannelAccess> iterator = channelAccesses.iterator(); iterator.hasNext(); ) {
            TerminalServiceChannelAccess channelAccess = iterator.next();
            registerTerminalService(channelAccess);
        }
        finalizeConfig();
    }

    protected abstract void finalizeConfig();

    protected abstract void initConfig();

    protected abstract void registerTerminalService(TerminalServiceChannelAccess channelAccess);

    protected final Message invokeService(TerminalServiceChannelAccess service, Message message) {
        producerTemplate.callService(service.getTerminalServiceAccess().getService(), message);
        return message;
    }

    public static String getProtocolKey() {
        return null;
    }

}

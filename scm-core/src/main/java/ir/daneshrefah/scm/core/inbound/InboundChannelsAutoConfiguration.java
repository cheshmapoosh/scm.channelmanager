package ir.daneshrefah.scm.core.inbound;

import ir.daneshrefah.scm.common.model.service.JavaServiceImplementation;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.RestChannel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.service.ChannelService;
import ir.daneshrefah.scm.service.TerminalService;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */

@Configuration
public class InboundChannelsAutoConfiguration {

    @Autowired
    private ConfigurableBeanFactory beanFactory;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private TerminalService terminalService;

//    @Bean
//    public List<AbstractInboundChannelGenerator> inboundChannelGenerator() {
//        List<AbstractInboundChannelGenerator> inboundChannelGenerators = new ArrayList<>();
//        inboundChannelGenerators.add(new RestInboundChannelGenerator());
//        return inboundChannelGenerators;
//    }

    @Bean
    public void restInboundChannelGenerator() {
        List<Channel> channelList = channelService.findChannelList();
        for (Iterator<Channel> iterator = channelList.iterator(); iterator.hasNext(); ) {
            Channel channel = iterator.next();
            List<TerminalServiceChannelAccess> terminalServiceChannelAccessList = terminalService.
                    findTerminalServiceChannelAccessByChannelId((String) channel.getId());
            AbstractInboundChannelGenerator inboundChannelGenerator = null;
            switch (channel.getProtocol()) {
                case REST:
                    inboundChannelGenerator = new RestInboundChannelGenerator(channel, terminalServiceChannelAccessList);
                    break;
                case JMS:
                    JavaServiceImplementation javaImplementation = new JavaServiceImplementation();
                    break;
                case RMI:
                    System.out.println("It's Wednesday.");
                    break;
                case JAVA:
                    System.out.println("It's Wednesday.");
                    break;
                default:
                    System.out.println("Invalid day of the week.");
                    break;
            }
            beanFactory.registerSingleton("inboundChannelGeneratorBean_" + channel.getCode(), inboundChannelGenerator);
        }

//        List<RestInboundChannelGenerator> inboundChannelGenerators = new ArrayList<>();
//        inboundChannelGenerators.add(new RestInboundChannelGenerator());
//        return inboundChannelGenerators;
    }
}

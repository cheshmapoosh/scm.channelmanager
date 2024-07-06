package ir.daneshrefah.scm.core.integration.inbound;

import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.service.channel.ChannelService;
import ir.daneshrefah.scm.common.service.terminal.TerminalService;
import ir.daneshrefah.scm.core.config.ApplicationProperties;
import ir.daneshrefah.scm.plugin.api.inbound.InboundChannelGenerator;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Configuration
public class InboundChannelsAutoConfiguration /*implements ApplicationContextAware */{

    private static final Logger LOGGER = LoggerFactory.getLogger(InboundChannelsAutoConfiguration.class);

    @Autowired
    private ChannelService channelService;
    @Autowired
    private TerminalService terminalService;


    @Bean
    public boolean registerInboundBeans(ApplicationProperties applicationProperties, List<MessageInterceptor> requestInterceptors) {
        LOGGER.info("*********************************************************************");
        LOGGER.info("*                                                                   *");
        LOGGER.info("*                     Register Inbound Channels                     *");
        LOGGER.info("*                                                                   *");
        LOGGER.info("*********************************************************************");
        List<String> activeChannelList = applicationProperties.getChannels();
        if (null == activeChannelList || activeChannelList.isEmpty()) {
            throw new RuntimeException("no active channel is defined in application config.");
        }
        List<Channel> channels = channelService.findAllChannels();
        for (Iterator<Channel> iterator = channels.iterator(); iterator.hasNext(); ) {

            Channel channel = iterator.next();
            if (!activeChannelList.contains(channel.getCode())) {
                continue;
            }
            LOGGER.info("init channel '{}' with protocol '{}', metadata {} for terminal '{}'.", channel.getCode(),
                    channel.getProtocol(), channel.getMetadata(), channel.getTerminal().getCode());
            /*TODO query is very slow and should be improved.*/
            List<TerminalServiceAccess> terminalServiceAccessList = terminalService.
                    findTerminalServiceAccessByTerminalId(channel.getTerminal().getId());
            LOGGER.info("'{}' terminalService found to register.", terminalServiceAccessList.size());
            if (terminalServiceAccessList.size() < 1) {
                LOGGER.warn("no terminalService found for channel '{}'. skip initialization.", channel.getCode());
                continue;
            }

            String className = extractChannelClassName(channel);
            LOGGER.debug("className '{}' configured for channel '{}'", className, channel.getCode());
            InboundChannelGenerator inboundChannelGenerator = ClassLoader.findBeanOrCreateInstanceOfClass(
                    className, InboundChannelGenerator.class);
            if (null == inboundChannelGenerator) {
                LOGGER.warn("error on create instance of inboundChannelGenerator found for channel '{}' with protocolCode '{}' with ClassName '{}'",
                        channel.getCode(), channel.getProtocol(), channel.getChannelClassName());
                continue;
            }

            boolean isContinue = inboundChannelGenerator.initConfig(channel, terminalServiceAccessList);
            if (!isContinue) {
                LOGGER.error("error found in channel '{}' initialization. skip initialization.", channel.getCode());
                continue;
            }
            LOGGER.info("channel '{}' initialization completed successfully.", channel.getCode());
        }

        LOGGER.info("=================== end InboundChannelsAutoConfiguration ===================");
        return true;
    }

    private String extractChannelClassName(Channel channel) {
        if (null == channel || null == channel.getProtocol()) {
            return null;
        }
        switch (channel.getProtocol()) {
            case SPRING_REST:
                return "bean:springRestInboundChanelGenerator";
            case DYNAMIC_REST:
                return "bean:dynamicRestInboundChanelGenerator";
            case SOAP:
                System.out.println("The color is blue.");
                break;
            case JMS:
                System.out.println("The color is blue.");
                break;
            case CUSTOM:
                return channel.getChannelClassName();
            default:
                return null;
        }
        return null;
    }

}

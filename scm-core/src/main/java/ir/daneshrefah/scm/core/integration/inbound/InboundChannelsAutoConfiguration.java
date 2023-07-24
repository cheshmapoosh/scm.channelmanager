package ir.daneshrefah.scm.core.integration.inbound;

import ir.daneshrefah.scm.core.service.ChannelService;
import ir.daneshrefah.scm.core.service.TerminalService;
import ir.daneshrefah.scm.plugin.api.inbound.AbstractInboundChannelGenerator;
import ir.daneshrefah.scm.plugin.api.model.terminal.Channel;
import ir.daneshrefah.scm.plugin.api.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.utils.io.ClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */
@Configuration
public class InboundChannelsAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(InboundChannelsAutoConfiguration.class);

    @Autowired
    private ConfigurableBeanFactory beanFactory;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private TerminalService terminalService;

    @Bean
    public void restInboundChannelGenerator() {
        Map<String, Class<? extends AbstractInboundChannelGenerator>> inboundChannelGeneratorMap = initInboundChannelGeneratorMap();
        List<Channel> channelList = channelService.findChannelList();
        for (Iterator<Channel> iterator = channelList.iterator(); iterator.hasNext(); ) {
            Channel channel = iterator.next();
            Class<? extends AbstractInboundChannelGenerator> inboundChannelGeneratorClass = inboundChannelGeneratorMap.get(channel.getProtocolCode());
            if (null == inboundChannelGeneratorClass) {
                LOGGER.warn("No inboundChannelGenerator found for channel '{}' with protocolCode '{}'",
                        channel.getCode(), channel.getProtocolCode());
                continue;
            }

            AbstractInboundChannelGenerator inboundChannelGenerator = ClassLoader.createInstanceOfClass(
                    inboundChannelGeneratorClass, channel);
            if (null == inboundChannelGenerator) {
                LOGGER.warn("Error on create instance of inboundChannelGenerator found for channel '{}' with protocolCode '{}' with ClassName '{}'",
                        channel.getCode(), channel.getProtocolCode(), inboundChannelGeneratorClass.getName());
                continue;
            }

            List<TerminalServiceChannelAccess> terminalServiceChannelAccessList = terminalService.
                    findTerminalServiceChannelAccessByChannelId(channel.getId());
            inboundChannelGenerator.setChannelAccesses(terminalServiceChannelAccessList);
            beanFactory.registerSingleton("inboundChannelGeneratorBean_" + channel.getCode(), inboundChannelGenerator);
            LOGGER.info("inboundChannelGenerator '{}' for channel '{}' with protocolCode '{}' registration completed successfully.",
                    inboundChannelGeneratorClass.getName(), channel.getCode(), channel.getProtocolCode());
        }

    }

    private Map<String, Class<? extends AbstractInboundChannelGenerator>> initInboundChannelGeneratorMap() {
        List<Class<? extends AbstractInboundChannelGenerator>> classList = ClassLoader.loadSubclasses(AbstractInboundChannelGenerator.class, "ir.daneshrefah.scm");
        Map<String, Class<? extends AbstractInboundChannelGenerator>> resultMap = classList.stream()
                .collect(Collectors.toMap(cls -> {
                    try {
                        String key = (String) cls.getMethod("getProtocolKey").invoke(null);
                        LOGGER.info("InboundChannelGenerator '{}' with class '{}' registered.", key, cls.getName());
                        return key;
                    } catch (Exception e) {
                        LOGGER.error("error on initInboundChannelGeneratorMap.", e);
                        return null;
                    }
                }, cls -> cls));

        // Print the result map
        return resultMap;
    }

}

package ir.daneshrefah.scm.core.integration.inbound;

import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.core.service.ChannelService;
import ir.daneshrefah.scm.core.service.TerminalService;
import ir.daneshrefah.scm.core.service.TransformerService;
import ir.daneshrefah.scm.plugin.api.authority.decision.DecisionManager;
import ir.daneshrefah.scm.plugin.api.inbound.AbstractInboundChannelGenerator;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
public class InboundChannelsAutoConfiguration implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(InboundChannelsAutoConfiguration.class);

    @Autowired
    private ConfigurableBeanFactory beanFactory;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private TerminalService terminalService;
    @Autowired
    private ServiceProducerTemplate producerTemplate;
    @Autowired
    private DecisionManager decisionManager;
    @Autowired
    private TransformerService transformerService;
    private ApplicationContext applicationContext;

    @Bean
    public void registerInboundBeans() {
        LOGGER.info("=================== start InboundChannelsAutoConfiguration ===================");

//        Map<String, Class<? extends AbstractInboundChannelGenerator>> inboundChannelGeneratorMap = extractInboundChannelGeneratorMap();
//        LOGGER.info("found '{}' inboundChannelGenerator.", null != inboundChannelGeneratorMap ? inboundChannelGeneratorMap.size() : "null");

        LOGGER.info("start fetch channel list from database.");
        List<Channel> channels = channelService.findAllChannelList();
        LOGGER.info("found '{}' channel.", null != channels ? channels.size() : "null");

        for (Iterator<Channel> iterator = channels.iterator(); iterator.hasNext(); ) {
            Channel channel = iterator.next();
            LOGGER.info("start init channel with code '{}', with protocol '{}'.", channel.getCode(), channel.getChannelClassName());

//            Class<? extends AbstractInboundChannelGenerator> inboundChannelGeneratorClass = inboundChannelGeneratorMap.get(channel.getChannelClassName());
//            if (null == inboundChannelGeneratorClass) {
//                LOGGER.warn("No inboundChannelGenerator found for channel '{}' with protocolCode '{}'",
//                        channel.getCode(), channel.getChannelClassName());
//                continue;
//            }
//
//            AbstractInboundChannelGenerator inboundChannelGenerator = ClassLoader.createInstanceOfClass(
//                    inboundChannelGeneratorClass, applicationContext, producerTemplate, channel, terminalAuthorities);

            AbstractInboundChannelGenerator inboundChannelGenerator = ClassLoader.findBeanOrCreateInstanceOfClass(channel.getChannelClassName(),
                    AbstractInboundChannelGenerator.class);
            if (null == inboundChannelGenerator) {
                LOGGER.warn("Error on create instance of inboundChannelGenerator found for channel '{}' with protocolCode '{}' with ClassName '{}'",
                        channel.getCode(), channel.getChannelClassName(), channel.getChannelClassName());
                continue;
            }

            List<TerminalServiceChannelAccess> terminalServiceChannelAccessList = terminalService.
                    findTerminalServiceChannelAccessByChannelId(channel.getId());

            inboundChannelGenerator.initInbound(producerTemplate, decisionManager, channel, terminalServiceChannelAccessList,
                    transformerService);
//            inboundChannelGenerator.setChannelAccesses(terminalServiceChannelAccessList);
//            AbstractInboundChannelGenerator bean = (AbstractInboundChannelGenerator) beanFactory.getBean("inboundChannelGeneratorBean_" + channel.getCode());
//            bean.initInbound();
            LOGGER.info("inboundChannelGenerator '{}' for channel '{}' with protocolCode '{}' registration completed successfully.",
                    channel.getChannelClassName(), channel.getCode(), channel.getChannelClassName());

        }

        LOGGER.info("=================== end InboundChannelsAutoConfiguration ===================");
    }

//    private Map<String, Class<? extends AbstractInboundChannelGenerator>> extractInboundChannelGeneratorMap() {
//        List<Class<? extends AbstractInboundChannelGenerator>> classList = ClassLoader.loadSubclasses(AbstractInboundChannelGenerator.class, "ir.daneshrefah.scm");
//        Map<String, Class<? extends AbstractInboundChannelGenerator>> resultMap = classList.stream()
//                .collect(Collectors.toMap(cls -> {
//                    try {
//                        String key = (String) cls.getMethod("getProtocolKey").invoke(null);
//                        LOGGER.info("InboundChannelGenerator '{}' with class '{}' registered.", key, cls.getName());
//                        return key;
//                    } catch (Exception e) {
//                        LOGGER.error("error on initInboundChannelGeneratorMap.", e);
//                        return null;
//                    }
//                }, cls -> cls));
//
//        // Print the result map
//        return resultMap;
//    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

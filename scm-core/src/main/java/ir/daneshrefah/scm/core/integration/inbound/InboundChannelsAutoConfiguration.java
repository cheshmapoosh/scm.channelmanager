package ir.daneshrefah.scm.core.integration.inbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.service.ChannelService;
import ir.daneshrefah.scm.common.service.TerminalService;
import ir.daneshrefah.scm.core.config.ApplicationProperties;
import ir.daneshrefah.scm.core.integration.inbound.interceptor.AuthenticationInterceptor;
import ir.daneshrefah.scm.core.integration.inbound.interceptor.RequestValidationInterceptor;
import ir.daneshrefah.scm.core.integration.inbound.interceptor.TerminalRequestTransformerInterceptor;
import ir.daneshrefah.scm.core.integration.inbound.interceptor.TransactionAuthenticationInterceptor;
import ir.daneshrefah.scm.plugin.api.inbound.AbstractInboundChannelGenerator;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
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
    private ObjectMapper objectMapper;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private TerminalService terminalService;

    @Bean
    public List<MessageInterceptor> requestInterceptors(AuthenticationClientTemplate authenticationClientTemplate) {
        List<MessageInterceptor> result = Arrays.asList(new RequestValidationInterceptor(),
                new AuthenticationInterceptor(authenticationClientTemplate),
                new TransactionAuthenticationInterceptor(authenticationClientTemplate),
                new TerminalRequestTransformerInterceptor());
        return result;
    }

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
            AbstractInboundChannelGenerator inboundChannelGenerator = ClassLoader.findBeanOrCreateInstanceOfClass(
                    className, AbstractInboundChannelGenerator.class);
            if (null == inboundChannelGenerator) {
                LOGGER.warn("error on create instance of inboundChannelGenerator found for channel '{}' with protocolCode '{}' with ClassName '{}'",
                        channel.getCode(), channel.getProtocol(), channel.getChannelClassName());
                continue;
            }

            JsonNode jsonMetadata = null;
            try {
                if (StringUtils.isNotEmpty(channel.getMetadata())) {
                    jsonMetadata = objectMapper.readTree(channel.getMetadata());
                }
            } catch (JsonProcessingException e) {
                LOGGER.error("error parse metadata for channel '{}'. skip initialization.", channel.getCode(), e);
                continue;
            }

            boolean isContinue = inboundChannelGenerator.initConfig(requestInterceptors, Collections.emptyList(),
                    channel, jsonMetadata);
            if (!isContinue) {
                LOGGER.error("error found in channel '{}' initialization. skip initialization.", channel.getCode());
                continue;
            }
            LOGGER.info("channel '{}' initialization completed successfully.", channel.getCode());

            LOGGER.info("*** start register endpoints for channel '{}'", channel.getCode());
            isContinue = inboundChannelGenerator.registerEndpoints(terminalServiceAccessList);
            if (!isContinue) {
                LOGGER.error("error found in channel '{}' endpoint registration. skip registration.", channel.getCode());
                continue;
            }
            LOGGER.info("channel '{}' endpoint registration completed successfully.", channel.getCode());

        }
        /*LOGGER.info("=================== start InboundChannelsAutoConfiguration ===================");

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
*/
        LOGGER.info("=================== end InboundChannelsAutoConfiguration ===================");
        return true;
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

    /*@Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }*/

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

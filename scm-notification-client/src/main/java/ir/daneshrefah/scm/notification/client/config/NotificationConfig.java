package ir.daneshrefah.scm.notification.client.config;


//import com.zaxxer.hikari.HikariDataSource;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import ir.daneshrefah.scm.common.service.terminal.TerminalService;
import ir.daneshrefah.scm.notification.client.config.prop.ClientConfigProperties;
import ir.daneshrefah.scm.notification.client.jms.JakarataConnectionFactory;
import ir.daneshrefah.scm.notification.client.service.DisabledNotificationServiceImpl;
import ir.daneshrefah.scm.notification.client.service.MessageTemplateService;
import ir.daneshrefah.scm.notification.client.service.NotificationServiceImpl;
import ir.daneshrefah.scm.notification.client.service.provider.NotificationMessageProvider;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.notification.client.service.template.NotificationBodyProcessor;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsTemplate;

import java.util.List;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @apiNote <pre>{@code
 *  scm:
 *      notification:
 *          consumer:
 *              enabled: true
 *              strategy: database_queued
 *          producer:
 *              enabled: true
 *              mode: distributed
 * }</pre>
 * @since 2024-02-07
 */
@Slf4j
@Configuration
//@EnableConfigurationProperties({ClientConfigProperties.class})
@RequiredArgsConstructor
public class NotificationConfig {

    private static final int IBM_MQ_DEFAULT_PORT = 1414;
//    @Bean
//    public CommandLineRunner init() {
//        return args -> {
//            log.info(LogUtils.markWith(LogUtils.Color.GREEN, ">>> [notification-producer] configuration successfully initialized."));
//        };
//    }

    @Bean
    @ConditionalOnProperty(name = "scm.notification.enabled", havingValue = "false", matchIfMissing = true)
    public NotificationService notificationServiceDisabled() {
        return new DisabledNotificationServiceImpl();
    }

    @Bean
    @ConditionalOnProperty(name = "scm.notification.enabled", havingValue = "true")
    public NotificationService notificationService(
            TerminalService terminalService,
            MessageTemplateService messageTemplateService,
            List<NotificationBodyProcessor> bodyProcessors,
            List<NotificationMessageProvider> messageProviders/*,
            NotificationLogService notificationLogService,
            NotificationRepository notificationRepository*/) {
        return new NotificationServiceImpl(terminalService, messageTemplateService, bodyProcessors, messageProviders);
    }

    @Bean(name = "smsConnectionFactory")
    @ConditionalOnProperty(name = "scm.notification.sms.ibm-mq.host")
    public ConnectionFactory smsIbmMqConnectionFactory(ClientConfigProperties properties) throws JMSException {
        JakarataConnectionFactory factory = new JakarataConnectionFactory();
        factory.setQueueManager(properties.getSms().getIbmMq().getQueueManager());
        factory.setHostName(properties.getSms().getIbmMq().getHost());
        factory.setPort(Objects.nonNull(properties.getSms().getIbmMq().getPort()) ? properties.getSms().getIbmMq().getPort(): IBM_MQ_DEFAULT_PORT);
        factory.setTransportType(1);
        factory.setChannel(properties.getSms().getIbmMq().getChannel());
        factory.setUsername(properties.getSms().getIbmMq().getUsername());
        factory.setPassword(properties.getSms().getIbmMq().getPassword());

        return factory;
    }

    @Bean(name = "smsConnectionFactory")
    @ConditionalOnProperty(name = "scm.notification.sms.active-mq.host")
    public ConnectionFactory activeMqConnectionFactory() {
        return null;
    }

    @Bean(name = "smsJmsTemplate")
    @ConditionalOnProperty(name = "scm.notification.sms.enabled", havingValue = "true")
    public JmsTemplate smsJmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setDefaultDestinationName("CM2SMS_R");
        jmsTemplate.setTimeToLive(300000);
        jmsTemplate.setDeliveryMode(1);
        jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setExplicitQosEnabled(true);
        return jmsTemplate;
    }

}

package ir.daneshrefah.scm.notification.client.config;


//import com.zaxxer.hikari.HikariDataSource;

import ir.daneshrefah.scm.common.service.terminal.TerminalService;
import ir.daneshrefah.scm.mq.jms.JakarataConnectionFactory;
import ir.daneshrefah.scm.notification.client.config.prop.ClientConfigProperties;
import ir.daneshrefah.scm.notification.client.service.DisabledNotificationServiceImpl;
import ir.daneshrefah.scm.notification.client.service.MessageTemplateService;
import ir.daneshrefah.scm.notification.client.service.NotificationServiceImpl;
import ir.daneshrefah.scm.notification.client.service.provider.NotificationMessageProvider;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.notification.client.service.template.NotificationBodyProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


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

}

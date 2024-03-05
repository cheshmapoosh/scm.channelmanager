package ir.daneshrefah.scm.notification.client.config;


//import com.zaxxer.hikari.HikariDataSource;

import ir.daneshrefah.scm.common.data.repository.notification.NotificationRepository;
import ir.daneshrefah.scm.notification.client.service.MessageTemplateService;
import ir.daneshrefah.scm.notification.client.service.NotificationServiceImpl;
import ir.daneshrefah.scm.notification.client.service.log.NotificationLogService;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.notification.client.service.template.NotificationBodyProcessor;
import ir.daneshrefah.scm.utils.log.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @apiNote
 * <pre>{@code
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
@EnableConfigurationProperties
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scm.notification.producer.enabled",havingValue = "true")
public class NotificationConfig {

    @Bean
    public CommandLineRunner init() {
        return args -> {
            log.info(LogUtils.markWith(LogUtils.Color.GREEN, ">>> [notification-producer] configuration successfully initialized."));
        };
    }

    @Bean
    public NotificationService notificationService(
            List<NotificationBodyProcessor> bodyProcessors,
            NotificationLogService notificationLogService,
            MessageTemplateService messageTemplateService,
            NotificationRepository notificationRepository){
        return new NotificationServiceImpl(bodyProcessors,notificationLogService,messageTemplateService,notificationRepository);
    }


}

package ir.daneshrefah.scm.notification.consumer.config;

import ir.daneshrefah.scm.notification.consumer.exception.NotificationDistributedLockDisabledException;
import ir.daneshrefah.scm.utils.log.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ConditionalOnProperty(name = "scm.notification.consumer.enabled",havingValue = "true")
@Slf4j
@EnableScheduling
@EnableConfigurationProperties
@RequiredArgsConstructor
public class NotificationConfiguration {

    @Bean
    public CommandLineRunner commandLineRunner(@Value("${scm.cache.client.config.distributed}") Boolean cacheServerDistributionStatus){
        return args -> {
            if (!cacheServerDistributionStatus){
                log.error(LogUtils.markWith(LogUtils.Color.RED,">>> notification schedule needed '{} = enable' for distributed lock functionality."),"scm.cache.client.config.distributed");
                throw new NotificationDistributedLockDisabledException("notification distribution feature is locked");
            }
            log.info(LogUtils.markWith(LogUtils.Color.GREEN,">>> [notification-consumer] queue manager scheduled successfully."));
        };
    }


}

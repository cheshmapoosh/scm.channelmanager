package ir.daneshrefah.scm.notification.config;

import ir.daneshrefah.scm.notification.exception.NotificationDistributedLockDisabledException;
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

import static ir.daneshrefah.scm.notification.config.ConfigConstants.SCM_CACHE_DISTRIBUTION_STATUS;

@Configuration
@ConditionalOnProperty(name = "scm.notification.distributed",havingValue = "true")
@Slf4j
@EnableScheduling
@EnableConfigurationProperties
@RequiredArgsConstructor
public class NotificationConfiguration {

    @Bean
    public CommandLineRunner commandLineRunner(@Value("${"+SCM_CACHE_DISTRIBUTION_STATUS+"}") Boolean cacheServerDistributionStatus){
        return args -> {
            if (!cacheServerDistributionStatus){
                log.error(LogUtils.markWith(LogUtils.Color.RED,">>> notification schedule needed '{} = enable' for distributed lock functionality."),SCM_CACHE_DISTRIBUTION_STATUS);
                throw new NotificationDistributedLockDisabledException();
            }
            log.info(LogUtils.markWith(LogUtils.Color.GREEN,">>> [notification-core] queue manager scheduled successfully."));
        };
    }


}

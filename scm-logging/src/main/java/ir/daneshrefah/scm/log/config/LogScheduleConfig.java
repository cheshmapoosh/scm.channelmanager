package ir.daneshrefah.scm.log.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableScheduling
@EnableAsync
public class LogScheduleConfig {

    @Bean(name = "logSchedulerThreadPool")
    public Executor schedulerThreadPoolExecutor(
            @Value("${scm.log.logSchedulerThreadPool.corePoolSize:5}") Integer corePoolSize,
            @Value("${scm.log.logSchedulerThreadPool.maxPoolSize:5}") Integer maxPoolSize
    ) {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setThreadNamePrefix("Log-Scheduler-Thread-");
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}

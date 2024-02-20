package ir.daneshrefah.scm.notification.client.config.datasource;

import ir.daneshrefah.scm.notification.client.config.ConfigProperties;
import ir.daneshrefah.scm.notification.client.config.NotificationConfigProperties;
import ir.daneshrefah.scm.notification.client.service.DisabledNotificationServiceImpl;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.utils.log.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;

@ConditionalOnMissingBean(DataSourceConfig.class)
@EnableConfigurationProperties
@Configuration
@Slf4j
@RequiredArgsConstructor
@EnableTransactionManagement
@EnableJpaRepositories
        (
        basePackages = ConfigProperties.BASE_PACKAGE,
        entityManagerFactoryRef = ConfigProperties.ENTITY_MANAGER_FACTORY_REF,
        transactionManagerRef = ConfigProperties.TX_MANGER_REF_NAME
)
public class DisabledDataSourceConfig {

    private final DataSource dataSource;

    @Bean
    public CommandLineRunner init(NotificationConfigProperties properties){
        return args -> {
            if (Objects.nonNull(properties) && properties.isEnabled()){
                log.warn(LogUtils.markWith(LogUtils.Color.RED,">>> notification functionality is enabled but datasource config does not set"));
            }else {
                log.warn(LogUtils.markWith(LogUtils.Color.YELLOW,">>> notification functionality is disabled."));
            }
        };
    }

    @Bean
    public DataSource notificationDataSource() {
        return dataSource;
    }

    @Bean
    @Scope("singleton")
    public NotificationService notificationService(){
        return new DisabledNotificationServiceImpl();
    }


    @Bean
    public LocalContainerEntityManagerFactoryBean notificationEntityManagerFactory(
            @Qualifier(ConfigProperties.NOTIFICATION_DATA_SOURCE_BEAN) DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(dataSource)
                .packages(ConfigProperties.BASE_PACKAGE)
                .build();
    }

    @Bean
    public PlatformTransactionManager notificationTransactionManager(
            @Qualifier(ConfigProperties.ENTITY_MANAGER_FACTORY_REF) LocalContainerEntityManagerFactoryBean todosEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(todosEntityManagerFactory.getObject()));
    }

}

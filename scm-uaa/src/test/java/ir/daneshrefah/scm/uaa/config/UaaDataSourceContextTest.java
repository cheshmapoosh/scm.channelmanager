package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.uaa.repository.activation.UserActivationRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.UserRepository;
import ir.daneshrefah.scm.uaa.config.datasource.activation.ActivationDataSourceConfig;
import ir.daneshrefah.scm.uaa.config.datasource.authentication.MainDataSourceConfig;
import ir.daneshrefah.scm.uaa.service.messages.LoginMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UaaDataSourceContextTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    HibernateJpaAutoConfiguration.class,
                    TransactionAutoConfiguration.class
            ))
            .withUserConfiguration(MainDataSourceConfig.class, ActivationDataSourceConfig.class)
            .withPropertyValues(
                    "scm.uaa.datasource.main.enabled=true",
                    "scm.uaa.datasource.main.url=jdbc:h2:mem:uaa-main;MODE=DB2;DB_CLOSE_DELAY=-1",
                    "scm.uaa.datasource.main.username=sa",
                    "scm.uaa.datasource.main.password=",
                    "scm.uaa.datasource.main.driver-class-name=org.h2.Driver",
                    "scm.uaa.datasource.main.max-connection=2",
                    "scm.uaa.datasource.main.default-schema=PUBLIC",
                    "scm.uaa.datasource.activation.url=jdbc:h2:mem:uaa-activation;MODE=DB2;DB_CLOSE_DELAY=-1",
                    "scm.uaa.datasource.activation.username=sa",
                    "scm.uaa.datasource.activation.password=",
                    "scm.uaa.datasource.activation.driver-class-name=org.h2.Driver",
                    "scm.uaa.datasource.activation.max-connection=2",
                    "scm.uaa.datasource.activation.default-schema=PUBLIC",
                    "spring.jpa.hibernate.ddl-auto=none"
            );

    @Test
    void uaaDatasourceContextLoadsWithActivationEnabled() {
        contextRunner
                .withPropertyValues("scm.uaa.datasource.activation.enabled=true")
                .run(context -> {
                    assertNotNull(context.getBean("mainDataSource"));
                    assertNotNull(context.getBean("mainEntityManagerFactory"));
                    assertNotNull(context.getBean("mainTransactionManager"));
                    assertNotNull(context.getBean(UserRepository.class));
                    assertNotNull(context.getBean("activationDataSource"));
                    assertNotNull(context.getBean("activationEntityManagerFactory"));
                    assertNotNull(context.getBean("activationTransactionManager"));
                    assertNotNull(context.getBean(UserActivationRepository.class));
                });
    }

    @Test
    void uaaDatasourceContextLoadsWithActivationDisabled() {
        contextRunner
                .withPropertyValues("scm.uaa.datasource.activation.enabled=false")
                .run(context -> {
                    assertNotNull(context.getBean("mainDataSource"));
                    assertNotNull(context.getBean("mainEntityManagerFactory"));
                    assertNotNull(context.getBean("mainTransactionManager"));
                    assertNotNull(context.getBean(UserRepository.class));
                    assertFalse(context.containsBean("activationDataSource"));
                    assertFalse(context.containsBean("activationEntityManagerFactory"));
                    assertFalse(context.containsBean("activationTransactionManager"));
                    assertTrue(context.getBeansOfType(UserActivationRepository.class).isEmpty());
                });
    }

    @Test
    void activationDatasourceIsAbsentWhenItsUrlIsMissing() {
        new ApplicationContextRunner()
                .withUserConfiguration(ActivationDataSourceConfig.class)
                .withPropertyValues("scm.uaa.datasource.activation.enabled=true")
                .run(context -> assertFalse(context.containsBean("activationDataSource")));
    }

    @Test
    void activationDependentServiceIsAbsentWhenActivationIsDisabled() {
        contextRunner
                .withUserConfiguration(LoginMessageService.class)
                .withPropertyValues("scm.uaa.datasource.activation.enabled=false")
                .run(context -> assertTrue(context.getBeansOfType(LoginMessageService.class).isEmpty()));
    }

    @Test
    void activationDependentServiceIsCreatedWhenActivationIsEnabled() {
        contextRunner
                .withUserConfiguration(LoginMessageService.class)
                .withPropertyValues("scm.uaa.datasource.activation.enabled=true")
                .run(context -> assertNotNull(context.getBean(LoginMessageService.class)));
    }

    @Test
    void mainDatasourceCannotBeDisabled() {
        contextRunner
                .withPropertyValues(
                        "scm.uaa.datasource.main.enabled=false",
                        "scm.uaa.datasource.activation.enabled=false"
                )
                .run(context -> assertNotNull(context.getStartupFailure()));
    }
}

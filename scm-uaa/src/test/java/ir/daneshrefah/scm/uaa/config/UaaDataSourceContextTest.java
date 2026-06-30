package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.uaa.repository.activation.LoginMessageRepository;
import ir.daneshrefah.scm.uaa.service.messages.LoginMessageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UaaDataSourceContextTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(MainDataSourceConfig.class, ActivationDataSourceConfig.class)
            .withPropertyValues(
                    "scm.uaa.datasource.main.enabled=true",
                    "scm.uaa.datasource.main.url=jdbc:db2://localhost:50000/SCM",
                    "scm.uaa.datasource.main.username=scm",
                    "scm.uaa.datasource.main.password=",
                    "scm.uaa.datasource.main.driver-class-name=com.ibm.db2.jcc.DB2Driver",
                    "scm.uaa.datasource.main.max-connection=2",
                    "scm.uaa.datasource.activation.url=jdbc:db2://localhost:50000/SCM",
                    "scm.uaa.datasource.activation.username=scm",
                    "scm.uaa.datasource.activation.password=",
                    "scm.uaa.datasource.activation.driver-class-name=com.ibm.db2.jcc.DB2Driver",
                    "scm.uaa.datasource.activation.max-connection=2"
            );

    @Test
    void uaaDatasourceContextLoadsWithActivationEnabled() {
        contextRunner
                .withPropertyValues("scm.uaa.datasource.activation.enabled=true")
                .run(context -> {
                    assertNotNull(context.getBean("mainDataSource"));
                    assertNotNull(context.getBean("activationDataSource"));
                });
    }

    @Test
    void uaaDatasourceContextLoadsWithActivationDisabled() {
        contextRunner
                .withPropertyValues("scm.uaa.datasource.activation.enabled=false")
                .run(context -> {
                    assertNotNull(context.getBean("mainDataSource"));
                    assertFalse(context.containsBean("activationDataSource"));
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
                .withBean(LoginMessageRepository.class, () -> Mockito.mock(LoginMessageRepository.class))
                .withPropertyValues("scm.uaa.datasource.activation.enabled=false")
                .run(context -> assertTrue(context.getBeansOfType(LoginMessageService.class).isEmpty()));
    }

    @Test
    void activationDependentServiceIsCreatedWhenActivationIsEnabled() {
        contextRunner
                .withUserConfiguration(LoginMessageService.class)
                .withBean(LoginMessageRepository.class, () -> Mockito.mock(LoginMessageRepository.class))
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

package ir.daneshrefah.scm.provider.task.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.persistence.JpaManagedPackageContributor;
import ir.daneshrefah.scm.provider.task.api.ProcessInstanceService;
import ir.daneshrefah.scm.provider.task.api.TaskInstanceService;
import ir.daneshrefah.scm.provider.task.camel.TaskProviderComponent;
import ir.daneshrefah.scm.provider.task.camel.TaskProviderOperationAdapter;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Set;

@AutoConfiguration(afterName = "ir.daneshrefah.scm.uaa.client.autoconfigure.ScmResourceServerAutoConfiguration")
@ConditionalOnProperty(
        prefix = "scm.provider.task.jpa",
        name = "enabled",
        havingValue = "true"
)
@ComponentScan(basePackages = {
        "ir.daneshrefah.scm.provider.task.api",
        "ir.daneshrefah.scm.provider.task.event",
        "ir.daneshrefah.scm.provider.task.mapper",
        "ir.daneshrefah.scm.provider.task.service"
})
@EnableJpaRepositories(
        basePackages = "ir.daneshrefah.scm.provider.task.repository",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class ScmTaskProviderAutoConfiguration {

    // TODO Replace focused provider scanning with explicit bean registration as the provider surface stabilizes.
    @Bean
    @ConditionalOnMissingBean(name = "taskProviderJpaManagedPackageContributor")
    public JpaManagedPackageContributor taskProviderJpaManagedPackageContributor() {
        return () -> Set.of("ir.daneshrefah.scm.provider.task.entity");
    }

    @Bean
    @ConditionalOnClass(CamelContext.class)
    @ConditionalOnMissingBean
    public TaskProviderOperationAdapter taskProviderOperationAdapter(
            ObjectMapper objectMapper,
            ProcessInstanceService processInstanceService,
            TaskInstanceService taskInstanceService
    ) {
        return new TaskProviderOperationAdapter(
                objectMapper,
                processInstanceService,
                taskInstanceService
        );
    }

    @Bean(name = TaskProviderComponent.SCHEME)
    @ConditionalOnClass(CamelContext.class)
    @ConditionalOnMissingBean(name = TaskProviderComponent.SCHEME)
    public TaskProviderComponent taskProviderComponent(
            TaskProviderOperationAdapter operationAdapter,
            ObjectProvider<CamelContext> camelContextProvider
    ) {
        CamelContext camelContext = camelContextProvider.getIfAvailable();
        return camelContext == null
                ? new TaskProviderComponent(operationAdapter)
                : new TaskProviderComponent(camelContext, operationAdapter);
    }
}

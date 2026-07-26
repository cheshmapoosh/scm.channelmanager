package ir.daneshrefah.scm.provider.task.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.persistence.JpaManagedPackageContributor;
import ir.daneshrefah.scm.provider.task.api.ProcessInstanceService;
import ir.daneshrefah.scm.provider.task.api.TaskInstanceService;
import ir.daneshrefah.scm.provider.task.camel.TaskProviderComponent;
import ir.daneshrefah.scm.provider.task.camel.TaskProviderOperationAdapter;
import ir.daneshrefah.scm.provider.task.workflow.InternalTaskWorkflowEngine;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowEngine;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowEngineRegistry;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowStepTypeResolver;
import jakarta.persistence.EntityManagerFactory;
import org.apache.camel.CamelContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Set;

@AutoConfiguration(afterName = "ir.daneshrefah.scm.uaa.client.autoconfigure.ScmResourceServerAutoConfiguration")
@EnableConfigurationProperties(ScmTaskProviderProperties.class)
@ConditionalOnProperty(
        prefix = "scm.provider.task",
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
        entityManagerFactoryRef = "scmTaskProviderEntityManagerFactory",
        transactionManagerRef = "scmTaskProviderTransactionManager"
)
public class ScmTaskProviderAutoConfiguration {

    static final String TASK_PROVIDER_ENTITY_MANAGER_FACTORY = "scmTaskProviderEntityManagerFactory";
    static final String TASK_PROVIDER_TRANSACTION_MANAGER = "scmTaskProviderTransactionManager";
    static final String TASK_PROVIDER_ENTITY_PACKAGE = "ir.daneshrefah.scm.provider.task.entity";

    private static final String MODE_GUIDANCE = "Either remove both properties to use default task provider persistence, "
            + "or configure both entity-manager-factory and transaction-manager to use dedicated task provider persistence.";

    @Bean
    static TaskProviderPersistenceBeanRegistryPostProcessor taskProviderPersistenceBeanRegistryPostProcessor() {
        return new TaskProviderPersistenceBeanRegistryPostProcessor();
    }

    // TODO Replace focused provider scanning with explicit bean registration as the provider surface stabilizes.
    @Bean
    @Conditional(TaskProviderPrimaryPersistenceCondition.class)
    @ConditionalOnMissingBean(name = "taskProviderJpaManagedPackageContributor")
    public JpaManagedPackageContributor taskProviderJpaManagedPackageContributor() {
        return () -> Set.of(TASK_PROVIDER_ENTITY_PACKAGE);
    }

    @Bean
    @ConditionalOnClass(CamelContext.class)
    @ConditionalOnMissingBean
    public TaskWorkflowStepTypeResolver taskWorkflowStepTypeResolver() {
        return new TaskWorkflowStepTypeResolver();
    }

    @Bean
    @ConditionalOnClass(CamelContext.class)
    @ConditionalOnMissingBean
    public InternalTaskWorkflowEngine internalTaskWorkflowEngine(
            ObjectMapper objectMapper,
            ProcessInstanceService processInstanceService,
            TaskInstanceService taskInstanceService
    ) {
        return new InternalTaskWorkflowEngine(
                objectMapper,
                processInstanceService,
                taskInstanceService
        );
    }

    @Bean
    @ConditionalOnClass(CamelContext.class)
    @ConditionalOnMissingBean
    public TaskWorkflowEngineRegistry taskWorkflowEngineRegistry(
            ScmTaskProviderProperties properties,
            ObjectProvider<TaskWorkflowEngine> engines
    ) {
        List<TaskWorkflowEngine> engineList = engines.orderedStream().toList();
        return new TaskWorkflowEngineRegistry(properties, engineList);
    }

    @Bean
    @ConditionalOnClass(CamelContext.class)
    @ConditionalOnMissingBean
    public TaskProviderOperationAdapter taskProviderOperationAdapter(
            TaskWorkflowStepTypeResolver stepTypeResolver,
            TaskWorkflowEngineRegistry engineRegistry
    ) {
        return new TaskProviderOperationAdapter(stepTypeResolver, engineRegistry);
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

    static class TaskProviderPrimaryPersistenceCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return !ScmTaskProviderProperties.from(context.getEnvironment()).hasDedicatedPersistence();
        }
    }

    static class TaskProviderPersistenceBeanRegistryPostProcessor
            implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, Ordered {

        private Environment environment;

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            ScmTaskProviderProperties properties = properties();

            if (!properties.isEnabled() || properties.hasIncompleteDedicatedPersistence()) {
                return;
            }

            registerAlias(
                    registry,
                    properties.resolvedEntityManagerFactoryBeanName(),
                    TASK_PROVIDER_ENTITY_MANAGER_FACTORY
            );
            registerAlias(
                    registry,
                    properties.resolvedTransactionManagerBeanName(),
                    TASK_PROVIDER_TRANSACTION_MANAGER
            );
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            ScmTaskProviderProperties properties = properties();

            if (!properties.isEnabled()) {
                return;
            }

            if (properties.hasIncompleteDedicatedPersistence()) {
                throw invalidConfiguration(MODE_GUIDANCE);
            }

            validateBean(
                    beanFactory,
                    properties.resolvedEntityManagerFactoryBeanName(),
                    EntityManagerFactory.class,
                    properties.hasDedicatedPersistence()
                            ? "configured task provider EntityManagerFactory"
                            : "default task provider EntityManagerFactory"
            );
            validateBean(
                    beanFactory,
                    properties.resolvedTransactionManagerBeanName(),
                    PlatformTransactionManager.class,
                    properties.hasDedicatedPersistence()
                            ? "configured task provider TransactionManager"
                            : "default task provider TransactionManager"
            );
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }

        private ScmTaskProviderProperties properties() {
            return ScmTaskProviderProperties.from(environment);
        }

        private void registerAlias(BeanDefinitionRegistry registry, String beanName, String alias) {
            if (beanName.equals(alias)) {
                return;
            }
            if (registry.containsBeanDefinition(alias)) {
                throw invalidConfiguration("Cannot register internal task provider persistence alias '" + alias
                        + "' for bean '" + beanName + "' because a bean with the alias name already exists. "
                        + MODE_GUIDANCE);
            }
            registry.registerAlias(beanName, alias);
        }

        private void validateBean(
                ConfigurableListableBeanFactory beanFactory,
                String beanName,
                Class<?> expectedType,
                String description
        ) {
            if (!beanFactory.containsBean(beanName)) {
                throw invalidConfiguration(description + " bean '" + beanName + "' was not found. " + MODE_GUIDANCE);
            }

            if (beanFactory.isTypeMatch(beanName, expectedType)) {
                return;
            }

            Class<?> beanType = beanFactory.getType(beanName, false);
            if (beanType == null || isCompatibleType(beanType, expectedType)) {
                return;
            }

            throw invalidConfiguration(description + " bean '" + beanName + "' must be a "
                    + expectedType.getName() + " but was " + beanType.getName() + ". " + MODE_GUIDANCE);
        }

        private boolean isCompatibleType(Class<?> beanType, Class<?> expectedType) {
            if (expectedType.isAssignableFrom(beanType)) {
                return true;
            }
            return EntityManagerFactory.class.equals(expectedType)
                    && EntityManagerFactoryInfo.class.isAssignableFrom(beanType);
        }

        private IllegalStateException invalidConfiguration(String message) {
            return new IllegalStateException("Invalid scm.provider.task persistence configuration. " + message);
        }
    }
}

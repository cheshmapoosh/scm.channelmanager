package ir.daneshrefah.scm.provider.task.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.persistence.JpaManagedPackageContributor;
import ir.daneshrefah.scm.provider.task.api.ProcessInstanceService;
import ir.daneshrefah.scm.provider.task.api.TaskInstanceService;
import ir.daneshrefah.scm.provider.task.camel.TaskProviderComponent;
import ir.daneshrefah.scm.provider.task.camel.TaskProviderOperationAdapter;
import jakarta.persistence.EntityManagerFactory;
import org.apache.camel.CamelContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationDelegate;
import org.springframework.data.repository.config.RepositoryConfigurationUtils;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@AutoConfiguration(afterName = "ir.daneshrefah.scm.uaa.client.autoconfigure.ScmResourceServerAutoConfiguration")
@EnableConfigurationProperties(ScmTaskProviderProperties.class)
@ConditionalOnProperty(
        prefix = "scm.provider.task",
        name = "enabled",
        havingValue = "true"
)
@Import(ScmTaskProviderAutoConfiguration.TaskProviderJpaRepositoriesRegistrar.class)
@ComponentScan(basePackages = {
        "ir.daneshrefah.scm.provider.task.api",
        "ir.daneshrefah.scm.provider.task.event",
        "ir.daneshrefah.scm.provider.task.mapper",
        "ir.daneshrefah.scm.provider.task.service"
})
public class ScmTaskProviderAutoConfiguration {

    static final String DEFAULT_ENTITY_MANAGER_FACTORY = "entityManagerFactory";
    static final String DEFAULT_TRANSACTION_MANAGER = "transactionManager";
    static final String TASK_PROVIDER_REPOSITORY_PACKAGE = "ir.daneshrefah.scm.provider.task.repository";
    static final String TASK_PROVIDER_ENTITY_PACKAGE = "ir.daneshrefah.scm.provider.task.entity";

    @Bean
    static TaskProviderPersistenceValidator taskProviderPersistenceValidator() {
        return new TaskProviderPersistenceValidator();
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

    static class TaskProviderJpaRepositoriesRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

        private ResourceLoader resourceLoader;
        private Environment environment;

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

        @Override
        public void registerBeanDefinitions(
                AnnotationMetadata importingClassMetadata,
                BeanDefinitionRegistry registry,
                BeanNameGenerator importBeanNameGenerator
        ) {
            TaskProviderPersistenceSettings settings = TaskProviderPersistenceSettings.from(environment);

            if (!settings.enabled() || settings.hasIncompleteDedicatedPersistence()) {
                return;
            }

            Map<String, Object> repositoryAttributes = repositoryAttributes(settings);

            AnnotationRepositoryConfigurationSource configurationSource = new AnnotationRepositoryConfigurationSource(
                    new TaskProviderJpaRepositoriesMetadata(importingClassMetadata, repositoryAttributes),
                    EnableJpaRepositories.class,
                    resourceLoader,
                    environment,
                    registry,
                    importBeanNameGenerator
            );
            JpaRepositoryConfigExtension extension = new JpaRepositoryConfigExtension();
            RepositoryConfigurationUtils.exposeRegistration(extension, registry, configurationSource);
            RepositoryConfigurationDelegate delegate = new RepositoryConfigurationDelegate(
                    configurationSource,
                    resourceLoader,
                    environment
            );
            delegate.registerRepositoriesIn(registry, extension);
        }

        private Map<String, Object> repositoryAttributes(TaskProviderPersistenceSettings settings) {
            Map<String, Object> attributes = new LinkedHashMap<>();
            for (Method method : EnableJpaRepositories.class.getDeclaredMethods()) {
                attributes.put(method.getName(), method.getDefaultValue());
            }
            attributes.put("basePackages", new String[]{TASK_PROVIDER_REPOSITORY_PACKAGE});
            attributes.put("includeFilters", new AnnotationAttributes[0]);
            attributes.put("excludeFilters", new AnnotationAttributes[0]);
            attributes.put("entityManagerFactoryRef", settings.entityManagerFactoryBeanName());
            attributes.put("transactionManagerRef", settings.transactionManagerBeanName());
            return attributes;
        }
    }

    static class TaskProviderJpaRepositoriesMetadata implements AnnotationMetadata {

        private final AnnotationMetadata delegate;
        private final Map<String, Object> enableJpaRepositoriesAttributes;

        TaskProviderJpaRepositoriesMetadata(
                AnnotationMetadata delegate,
                Map<String, Object> enableJpaRepositoriesAttributes
        ) {
            this.delegate = delegate;
            this.enableJpaRepositoriesAttributes = enableJpaRepositoriesAttributes;
        }

        @Override
        public Map<String, Object> getAnnotationAttributes(String annotationName, boolean classValuesAsString) {
            if (EnableJpaRepositories.class.getName().equals(annotationName)) {
                return new LinkedHashMap<>(enableJpaRepositoriesAttributes);
            }
            return delegate.getAnnotationAttributes(annotationName, classValuesAsString);
        }

        @Override
        public MergedAnnotations getAnnotations() {
            return delegate.getAnnotations();
        }

        @Override
        public Set<MethodMetadata> getAnnotatedMethods(String annotationName) {
            return delegate.getAnnotatedMethods(annotationName);
        }

        @Override
        public Set<MethodMetadata> getDeclaredMethods() {
            return delegate.getDeclaredMethods();
        }

        @Override
        public String getClassName() {
            return delegate.getClassName();
        }

        @Override
        public boolean isInterface() {
            return delegate.isInterface();
        }

        @Override
        public boolean isAnnotation() {
            return delegate.isAnnotation();
        }

        @Override
        public boolean isAbstract() {
            return delegate.isAbstract();
        }

        @Override
        public boolean isFinal() {
            return delegate.isFinal();
        }

        @Override
        public boolean isIndependent() {
            return delegate.isIndependent();
        }

        @Override
        public String getEnclosingClassName() {
            return delegate.getEnclosingClassName();
        }

        @Override
        public String getSuperClassName() {
            return delegate.getSuperClassName();
        }

        @Override
        public String[] getInterfaceNames() {
            return delegate.getInterfaceNames();
        }

        @Override
        public String[] getMemberClassNames() {
            return delegate.getMemberClassNames();
        }
    }

    static class TaskProviderPrimaryPersistenceCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return !TaskProviderPersistenceSettings.from(context.getEnvironment()).hasDedicatedPersistence();
        }
    }

    static class TaskProviderPersistenceValidator implements BeanFactoryPostProcessor, EnvironmentAware, Ordered {

        private Environment environment;

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            TaskProviderPersistenceSettings settings = TaskProviderPersistenceSettings.from(environment);

            if (!settings.enabled()) {
                return;
            }

            if (settings.hasIncompleteDedicatedPersistence()) {
                throw invalidConfiguration("If any of scm.provider.task.datasource, "
                        + "scm.provider.task.entity-manager-factory, or scm.provider.task.transaction-manager "
                        + "is configured, all three must be configured.");
            }

            if (settings.hasDedicatedPersistence()) {
                validateBean(beanFactory, settings.datasource(), DataSource.class, "scm.provider.task.datasource");
                validateBean(
                        beanFactory,
                        settings.entityManagerFactory(),
                        EntityManagerFactory.class,
                        "scm.provider.task.entity-manager-factory"
                );
                validateBean(
                        beanFactory,
                        settings.transactionManager(),
                        PlatformTransactionManager.class,
                        "scm.provider.task.transaction-manager"
                );
            } else {
                validateBean(
                        beanFactory,
                        DEFAULT_ENTITY_MANAGER_FACTORY,
                        EntityManagerFactory.class,
                        "default task provider EntityManagerFactory"
                );
                validateBean(
                        beanFactory,
                        DEFAULT_TRANSACTION_MANAGER,
                        PlatformTransactionManager.class,
                        "default task provider TransactionManager"
                );
            }
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }

        private void validateBean(
                ConfigurableListableBeanFactory beanFactory,
                String beanName,
                Class<?> expectedType,
                String description
        ) {
            if (!beanFactory.containsBean(beanName)) {
                throw invalidConfiguration(description + " bean '" + beanName + "' was not found.");
            }

            if (beanFactory.isTypeMatch(beanName, expectedType)) {
                return;
            }

            Class<?> beanType = beanFactory.getType(beanName, false);
            if (beanType == null || isCompatibleFactoryBean(beanType, expectedType)) {
                return;
            }

            throw invalidConfiguration(description + " bean '" + beanName + "' must be a "
                    + expectedType.getName() + " but was " + beanType.getName() + ".");
        }

        private boolean isCompatibleFactoryBean(Class<?> beanType, Class<?> expectedType) {
            if (expectedType.isAssignableFrom(beanType)) {
                return true;
            }
            if (EntityManagerFactory.class.equals(expectedType)
                    && EntityManagerFactoryInfo.class.isAssignableFrom(beanType)) {
                return true;
            }
            return FactoryBean.class.isAssignableFrom(beanType);
        }

        private IllegalStateException invalidConfiguration(String message) {
            return new IllegalStateException("Invalid scm.provider.task persistence configuration. " + message);
        }
    }

    record TaskProviderPersistenceSettings(
            boolean enabled,
            String datasource,
            String entityManagerFactory,
            String transactionManager
    ) {

        static TaskProviderPersistenceSettings from(Environment environment) {
            return new TaskProviderPersistenceSettings(
                    environment.getProperty("scm.provider.task.enabled", Boolean.class, false),
                    environment.getProperty("scm.provider.task.datasource"),
                    environment.getProperty("scm.provider.task.entity-manager-factory"),
                    environment.getProperty("scm.provider.task.transaction-manager")
            );
        }

        boolean hasDedicatedPersistence() {
            return StringUtils.hasText(datasource)
                    || StringUtils.hasText(entityManagerFactory)
                    || StringUtils.hasText(transactionManager);
        }

        boolean hasIncompleteDedicatedPersistence() {
            return hasDedicatedPersistence()
                    && !(StringUtils.hasText(datasource)
                    && StringUtils.hasText(entityManagerFactory)
                    && StringUtils.hasText(transactionManager));
        }

        String entityManagerFactoryBeanName() {
            return hasDedicatedPersistence() ? entityManagerFactory : DEFAULT_ENTITY_MANAGER_FACTORY;
        }

        String transactionManagerBeanName() {
            return hasDedicatedPersistence() ? transactionManager : DEFAULT_TRANSACTION_MANAGER;
        }
    }
}

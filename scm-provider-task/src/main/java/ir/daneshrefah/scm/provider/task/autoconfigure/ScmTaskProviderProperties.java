package ir.daneshrefah.scm.provider.task.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@ConfigurationProperties("scm.provider.task")
public class ScmTaskProviderProperties {

    static final String DEFAULT_ENTITY_MANAGER_FACTORY = "entityManagerFactory";
    static final String DEFAULT_TRANSACTION_MANAGER = "transactionManager";
    private static final String PREFIX = "scm.provider.task";

    private boolean enabled;
    private String entityManagerFactory;
    private String transactionManager;

    static ScmTaskProviderProperties from(Environment environment) {
        return Binder.get(environment)
                .bind(PREFIX, ScmTaskProviderProperties.class)
                .orElseGet(ScmTaskProviderProperties::new);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public void setEntityManagerFactory(String entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public String getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(String transactionManager) {
        this.transactionManager = transactionManager;
    }

    public boolean hasDedicatedPersistence() {
        return StringUtils.hasText(entityManagerFactory)
                || StringUtils.hasText(transactionManager);
    }

    public boolean hasIncompleteDedicatedPersistence() {
        return hasDedicatedPersistence()
                && !(StringUtils.hasText(entityManagerFactory) && StringUtils.hasText(transactionManager));
    }

    public String resolvedEntityManagerFactoryBeanName() {
        return StringUtils.hasText(entityManagerFactory)
                ? entityManagerFactory.trim()
                : DEFAULT_ENTITY_MANAGER_FACTORY;
    }

    public String resolvedTransactionManagerBeanName() {
        return StringUtils.hasText(transactionManager)
                ? transactionManager.trim()
                : DEFAULT_TRANSACTION_MANAGER;
    }
}

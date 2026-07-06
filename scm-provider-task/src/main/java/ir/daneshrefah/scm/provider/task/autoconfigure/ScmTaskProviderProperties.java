package ir.daneshrefah.scm.provider.task.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties("scm.provider.task")
public class ScmTaskProviderProperties {

    private boolean enabled;
    private String datasource;
    private String entityManagerFactory;
    private String transactionManager;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
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
        return StringUtils.hasText(datasource)
                || StringUtils.hasText(entityManagerFactory)
                || StringUtils.hasText(transactionManager);
    }
}

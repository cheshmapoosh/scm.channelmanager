package ir.daneshrefah.scm.provider.task.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@ConfigurationProperties("scm.provider.task")
public class ScmTaskProviderProperties {

    static final String DEFAULT_ENTITY_MANAGER_FACTORY = "entityManagerFactory";
    static final String DEFAULT_TRANSACTION_MANAGER = "transactionManager";
    private static final String PREFIX = "scm.provider.task";
    private static final String INTERNAL_PROVIDER_CODE = "internal";
    private static final String INTERNAL_ENGINE_TYPE = "internal";

    @Setter
    private boolean enabled;
    @Setter
    private String entityManagerFactory;
    @Setter
    private String transactionManager;
    private Map<String, TaskProviderInstanceProperties> providers = new LinkedHashMap<>();

    static ScmTaskProviderProperties from(Environment environment) {
        return Binder.get(environment)
                .bind(PREFIX, ScmTaskProviderProperties.class)
                .orElseGet(ScmTaskProviderProperties::new);
    }

    public void setProviders(Map<String, TaskProviderInstanceProperties> providers) {
        this.providers = providers == null ? new LinkedHashMap<>() : providers;
    }

    public Map<String, TaskProviderInstanceProperties> resolvedProviders() {
        if (providers == null || providers.isEmpty()) {
            Map<String, TaskProviderInstanceProperties> defaults = new LinkedHashMap<>();
            TaskProviderInstanceProperties internal = new TaskProviderInstanceProperties();
            internal.setEnabled(true);
            internal.setEngineType(INTERNAL_ENGINE_TYPE);
            defaults.put(INTERNAL_PROVIDER_CODE, internal);
            return defaults;
        }
        return providers;
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

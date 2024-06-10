package ir.daneshrefah.scm.core.config;

import com.zaxxer.hikari.HikariDataSource;
import ir.daneshrefah.scm.plugin.api.config.DatasourceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-31
 */
@RequiredArgsConstructor
@EnableConfigurationProperties(ApplicationProperties.class)
@Configuration
public class DataSourceConfig implements BeanDefinitionRegistryPostProcessor {

    @Bean
    @Primary
    public DataSource primaryDataSource(ApplicationProperties applicationProperties) {
        DatasourceProperties datasourceProperties = applicationProperties.getDatasource().getPrimary();
        HikariDataSource dataSource = DataSourceBuilder.create(this.getClass().getClassLoader())
                .type(HikariDataSource.class)
                .url(datasourceProperties.getUrl())
                .driverClassName(datasourceProperties.getDriverClassName())
                .username(datasourceProperties.getUsername())
                .password(datasourceProperties.getPassword())
                .build();
        dataSource.setSchema(datasourceProperties.getDefaultSchema());
        Integer maximumPoolSize = datasourceProperties.getMaxConnection();
        if (null != maximumPoolSize) {
            dataSource.setMaximumPoolSize(maximumPoolSize);
        }
        return dataSource;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanRegistry) throws BeansException {
        Environment env = ((DefaultListableBeanFactory) beanRegistry).getBean(Environment.class);
        for (int i = 0; ; i++) {
            String dataSourceName = env.getProperty("scm.datasource.secondary["+ i + "].name");
            if (null == dataSourceName) {
                break;
            }
            String dataSourceUrl = env.getProperty("scm.datasource.secondary["+ i + "].url");
            String dataSourceUsername = env.getProperty("scm.datasource.secondary["+ i + "].username");
            String dataSourcePassword = env.getProperty("scm.datasource.secondary["+ i + "].password");
            String dataSourceDriverClassName = env.getProperty("scm.datasource.secondary["+ i + "].driver-class-name");
            String dataSourceDefaultSchema = env.getProperty("scm.datasource.secondary["+ i + "].default-schema");
            String dataSourceMaxConnection = env.getProperty("scm.datasource.secondary["+ i + "].max-connection");

            BeanDefinition dataSourceBeanDef = BeanDefinitionBuilder.genericBeanDefinition(HikariDataSource.class)
                    .addPropertyValue("jdbcUrl", dataSourceUrl)
                    .addPropertyValue("username", dataSourceUsername)
                    .addPropertyValue("password", dataSourcePassword)
                    .addPropertyValue("driverClassName", dataSourceDriverClassName)
                    .addPropertyValue("schema", dataSourceDefaultSchema)
                    .getBeanDefinition();
            dataSourceBeanDef.setPrimary(false);
            beanRegistry.registerBeanDefinition("datasource_" + dataSourceName, dataSourceBeanDef);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

}

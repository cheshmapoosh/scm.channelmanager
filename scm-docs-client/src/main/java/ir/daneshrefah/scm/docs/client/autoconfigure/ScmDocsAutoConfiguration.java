package ir.daneshrefah.scm.docs.client.autoconfigure;

import ir.daneshrefah.scm.docs.client.provider.ClasspathScmDocContentProvider;
import ir.daneshrefah.scm.docs.client.provider.ScmDocCatalogProvider;
import ir.daneshrefah.scm.docs.client.provider.ScmDocContentProvider;
import ir.daneshrefah.scm.docs.client.registry.ScmDocsRegistry;
import ir.daneshrefah.scm.docs.client.web.ScmDocsController;
import ir.daneshrefah.scm.docs.client.web.ScmDocsHtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.DispatcherServlet;

@AutoConfiguration
@ConditionalOnClass(DispatcherServlet.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "scm.docs", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ScmDocsProperties.class)
public class ScmDocsAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ScmDocsAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public ClasspathScmDocContentProvider classpathScmDocContentProvider(ScmDocsProperties properties,
                                                                         ResourceLoader resourceLoader) {
        log.info("SCM docs client classpath provider enabled basePath={} classpathRoot={} configuredDocuments={}",
                properties.normalizedBasePath(), properties.getClasspathRoot(), properties.getDocuments().size());
        return new ClasspathScmDocContentProvider(properties, resourceLoader);
    }

    @Bean
    @ConditionalOnMissingBean
    public ScmDocsRegistry scmDocsRegistry(ObjectProvider<ScmDocCatalogProvider> catalogProviders,
                                           ObjectProvider<ScmDocContentProvider> contentProviders) {
        var catalogProviderList = catalogProviders.orderedStream().toList();
        var contentProviderList = contentProviders.orderedStream().toList();
        log.info("SCM docs client registry initialized catalogProviders={} contentProviders={}",
                catalogProviderList.size(), contentProviderList.size());
        return new ScmDocsRegistry(catalogProviderList, contentProviderList);
    }

    @Bean
    @ConditionalOnMissingBean
    public ScmDocsHtmlRenderer scmDocsHtmlRenderer() {
        log.debug("SCM docs HTML renderer initialized");
        return new ScmDocsHtmlRenderer();
    }

    @Bean
    @ConditionalOnMissingBean
    public ScmDocsController scmDocsController(ScmDocsRegistry registry,
                                               ScmDocsHtmlRenderer htmlRenderer,
                                               ScmDocsProperties properties) {
        log.info("SCM docs client controller initialized basePath={} moduleCode={}",
                properties.normalizedBasePath(), properties.getModuleCode());
        return new ScmDocsController(registry, htmlRenderer, properties);
    }
}

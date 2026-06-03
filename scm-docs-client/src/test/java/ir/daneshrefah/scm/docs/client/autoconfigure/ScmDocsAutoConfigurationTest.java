package ir.daneshrefah.scm.docs.client.autoconfigure;

import ir.daneshrefah.scm.docs.client.registry.ScmDocsRegistry;
import ir.daneshrefah.scm.docs.client.web.ScmDocsController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ScmDocsAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withUserConfiguration(TestApplication.class);

    @Test
    void discoversAutoConfigurationFromSpringBoot3MetadataLocation() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        assertThat(classLoader.getResource("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"))
                .isNotNull();
        assertThat(classLoader.getResource("spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"))
                .isNull();

        contextRunner
                .run(context -> assertThat(context)
                        .hasSingleBean(ScmDocsController.class)
                        .hasSingleBean(ScmDocsRegistry.class));
    }

    @Test
    void createsExpectedBeansWhenEnabled() {
        contextRunner
                .withPropertyValues("scm.docs.enabled=true")
                .run(context -> assertThat(context)
                        .hasSingleBean(ScmDocsController.class)
                        .hasSingleBean(ScmDocsRegistry.class));
    }

    @Test
    void backsOffWhenDisabled() {
        contextRunner
                .withPropertyValues("scm.docs.enabled=false")
                .run(context -> assertThat(context)
                        .doesNotHaveBean(ScmDocsController.class)
                        .doesNotHaveBean(ScmDocsRegistry.class));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }
}

package ir.daneshrefah.scm.docs.client.autoconfigure;

import ir.daneshrefah.scm.docs.client.registry.ScmDocsRegistry;
import ir.daneshrefah.scm.docs.client.web.ScmDocsController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ScmDocsAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ScmDocsAutoConfiguration.class));

    @Test
    void backsOffWhenDisabled() {
        contextRunner
                .withPropertyValues("scm.docs.enabled=false")
                .run(context -> assertThat(context)
                        .doesNotHaveBean(ScmDocsController.class)
                        .doesNotHaveBean(ScmDocsRegistry.class));
    }
}

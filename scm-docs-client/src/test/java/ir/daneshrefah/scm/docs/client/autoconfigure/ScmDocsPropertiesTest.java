package ir.daneshrefah.scm.docs.client.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScmDocsPropertiesTest {

    @Test
    void apiDefaultsAreEnabledWithoutCachePolicy() {
        ScmDocsProperties properties = new ScmDocsProperties();

        assertThat(properties.getApi().isEnabled()).isTrue();
        assertThat(properties.getApi().isFailFast()).isFalse();
        assertThat(ScmDocsProperties.Api.class.getDeclaredFields())
                .extracting(field -> field.getName())
                .doesNotContain("cache");
    }
}

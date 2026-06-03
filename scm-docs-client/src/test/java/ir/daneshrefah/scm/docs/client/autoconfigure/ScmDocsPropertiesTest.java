package ir.daneshrefah.scm.docs.client.autoconfigure;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ScmDocsPropertiesTest {

    @Test
    void apiDefaultsAreEnabledAndCached() {
        ScmDocsProperties properties = new ScmDocsProperties();

        assertThat(properties.getApi().isEnabled()).isTrue();
        assertThat(properties.getApi().isFailFast()).isFalse();
        assertThat(properties.getApi().getCache().isEnabled()).isTrue();
        assertThat(properties.getApi().getCache().getTtl()).isEqualTo(Duration.ofSeconds(60));
        assertThat(properties.getApi().getCache().getMaximumSize()).isEqualTo(128);
    }
}

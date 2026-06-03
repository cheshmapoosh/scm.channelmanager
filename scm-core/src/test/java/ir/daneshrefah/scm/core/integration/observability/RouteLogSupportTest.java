package ir.daneshrefah.scm.core.integration.observability;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RouteLogSupportTest {
    @Test
    void failureMessageMasksSensitiveKeyValuePairs() {
        RuntimeException exception = new RuntimeException(
                "call failed token=abc123 password:secret\nretry");

        assertThat(RouteLogSupport.failureMessage(exception))
                .isEqualTo("call failed token=*** password=*** retry");
    }
}

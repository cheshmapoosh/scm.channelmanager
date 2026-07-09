package ir.daneshrefah.scm.observation.autoconfigure;

import ir.daneshrefah.scm.observation.starter.autoconfigure.ScmObservationAutoConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScmObservationAutoConfigurationTest {
    @Test
    void webMvcInterceptorIsNotPresentOrRegisteredByAutoConfiguration() {
        assertThrows(ClassNotFoundException.class,
                () -> Class.forName("ir.daneshrefah.scm.observation.web.ObservationWebMvcInterceptor"));
        assertFalse(Arrays.stream(ScmObservationAutoConfiguration.class.getDeclaredClasses())
                .anyMatch(type -> type.getName().contains("WebMvc")));
    }
}

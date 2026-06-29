package ir.daneshrefah.scm.observation.uaa;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UaaLogbackConfigurationTest {
    @Test
    void uaaLogbackUsesSharedJsonlAppendersAndMarkerSeparatedObservationFiles() throws Exception {
        String xml;
        try (var input = getClass().getClassLoader().getResourceAsStream("logback-spring.xml")) {
            xml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertTrue(xml.contains("META-INF/scm/logback/scm-log-properties.xml"));
        assertTrue(xml.contains("META-INF/scm/logback/scm-console-simple-appender.xml"));
        assertTrue(xml.contains("META-INF/scm/logback/scm-file-jsonl-appender.xml"));
        assertTrue(xml.contains("META-INF/scm/logback/scm-trace-jsonl-appender.xml"));
        assertTrue(xml.contains("META-INF/scm/logback/scm-audit-jsonl-appender.xml"));
        assertTrue(xml.contains("SCM_FILE_JSONL"));
        assertTrue(xml.contains("SCM_TRACE_JSONL"));
        assertTrue(xml.contains("SCM_AUDIT_JSONL"));
        assertTrue(xml.contains("defaultValue=\"INFO\""));
        assertFalse(xml.contains("PatternLayoutEncoder"));
        assertFalse(xml.contains("ROLLING_FILE"));
    }
}

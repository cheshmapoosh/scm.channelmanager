package ir.daneshrefah.scm.observation.uaa;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

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
        for (String property : List.of(
                "scm.uaa.log.level.root",
                "scm.uaa.log.level.application",
                "scm.uaa.log.level.spring",
                "scm.uaa.log.level.spring-security",
                "scm.uaa.log.level.hibernate",
                "scm.uaa.log.level.hikari",
                "scm.uaa.log.level.ibm-mq"
        )) {
            assertTrue(xml.contains("source=\"" + property + "\""), property);
        }
        String devProfile = profile(xml, "dev");
        assertTrue(devProfile.contains("SCM_CONSOLE_SIMPLE"));
        assertTrue(devProfile.contains("SCM_FILE_JSONL"));
        assertTrue(devProfile.contains("SCM_TRACE_JSONL"));
        assertTrue(devProfile.contains("SCM_AUDIT_JSONL"));

        String kubernetesProfiles = profile(xml, "test,pilot,prod");
        assertFalse(kubernetesProfiles.contains("SCM_CONSOLE_SIMPLE"));
        assertTrue(kubernetesProfiles.contains("SCM_FILE_JSONL"));
        assertTrue(kubernetesProfiles.contains("SCM_TRACE_JSONL"));
        assertTrue(kubernetesProfiles.contains("SCM_AUDIT_JSONL"));
        assertTrue(xml.contains("No profile is the documented local fallback"));
        assertTrue(xml.contains("defaultValue=\"INFO\""));
        assertFalse(xml.contains("PatternLayoutEncoder"));
        assertFalse(xml.contains("ROLLING_FILE"));
    }

    private String profile(String xml, String name) {
        String startTag = "<springProfile name=\"" + name + "\">";
        int start = xml.indexOf(startTag);
        int end = xml.indexOf("</springProfile>", start);
        assertTrue(start >= 0 && end > start, "missing profile " + name);
        return xml.substring(start, end);
    }
}

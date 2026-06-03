package ir.daneshrefah.scm.core.integration.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileAuditEventWriterTest {
    @TempDir
    Path tempDir;

    @Test
    void writesAuditEventsAsJsonLines() throws Exception {
        Path auditFile = tempDir.resolve("audit.ndjson");
        AuditProperties properties = new AuditProperties();
        properties.setOutputPath(auditFile.toString());
        FileAuditEventWriter writer = new FileAuditEventWriter(
                properties,
                new ObjectMapper().findAndRegisterModules());

        writer.write(new AuditEvent(
                Instant.parse("2026-06-01T00:00:00Z"),
                "trace-1",
                "span-1",
                "corr-1",
                "channel.mb",
                "mb",
                "card",
                "v2",
                "CARD_INQUIRY",
                "BEFORE",
                "SUCCESS",
                null,
                null,
                "route-card",
                "exchange-1"));

        String line = Files.readString(auditFile);
        assertEquals(1, Files.readAllLines(auditFile).size());
        assertTrue(line.contains("\"traceId\":\"trace-1\""));
        assertTrue(line.contains("\"spanId\":\"span-1\""));
        assertTrue(line.contains("\"serviceVersion\":\"v2\""));
        assertTrue(line.endsWith(System.lineSeparator()));
    }
}

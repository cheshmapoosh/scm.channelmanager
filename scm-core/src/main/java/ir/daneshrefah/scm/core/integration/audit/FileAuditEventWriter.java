package ir.daneshrefah.scm.core.integration.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Component
@RequiredArgsConstructor
public class FileAuditEventWriter implements AuditEventWriter {
    private final AuditProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public synchronized void write(AuditEvent event) {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            Path outputPath = Path.of(StringUtils.defaultIfBlank(
                    properties.getOutputPath(),
                    "log/scm/scm-web/audit.ndjson"));
            Path parent = outputPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            String line = objectMapper.writeValueAsString(event) + System.lineSeparator();
            Files.writeString(
                    outputPath,
                    line,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (Exception e) {
            throw new IllegalStateException("Could not write SCM audit event.", e);
        }
    }
}

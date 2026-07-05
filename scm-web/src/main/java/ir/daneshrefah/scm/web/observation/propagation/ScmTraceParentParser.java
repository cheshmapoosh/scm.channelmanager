package ir.daneshrefah.scm.web.observation.propagation;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ScmTraceParentParser {
    public Optional<ScmTraceParent> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String[] parts = value.trim().split("-");
        if (parts.length != 4) {
            return Optional.empty();
        }
        try {
            return Optional.of(new ScmTraceParent(parts[0], parts[1], parts[2], parts[3]));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}

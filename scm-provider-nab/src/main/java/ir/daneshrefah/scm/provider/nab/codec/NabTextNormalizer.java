package ir.daneshrefah.scm.provider.nab.codec;

import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import org.springframework.stereotype.Component;

@Component
public class NabTextNormalizer {

    public String normalize(String value, NabResolvedConfig config) {
        if (value == null || config == null || config.characterNormalization() == null
                || !config.characterNormalization().enabled()) {
            return value;
        }
        String normalized = value;
        for (var entry : config.characterNormalization().replacements().entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                normalized = normalized.replace(entry.getKey(), entry.getValue());
            }
        }
        return normalized;
    }
}

package ir.daneshrefah.scm.provider.nab.codec;

import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class NabRqUidGenerator {
    private final AtomicInteger counter = new AtomicInteger();

    public String generate(NabResolvedConfig config) {
        int length = Math.max(config.rqUid().length(), 1);
        String type = config.rqUid().type() == null ? "NUMERIC" : config.rqUid().type().toUpperCase(Locale.ROOT);
        String value = "UUID".equals(type)
                ? UUID.randomUUID().toString().replace("-", "")
                : numericValue(length);
        if (value.length() > length) {
            return value.substring(value.length() - length);
        }
        if (value.length() < length) {
            return "0".repeat(length - value.length()) + value;
        }
        return value;
    }

    private String numericValue(int length) {
        String value = System.currentTimeMillis() + "%03d".formatted(Math.floorMod(counter.incrementAndGet(), 1000));
        if (value.length() < length) {
            value = value + System.nanoTime();
        }
        return value.replaceAll("\\D", "");
    }
}

package ir.daneshrefah.scm.observation;

import java.security.SecureRandom;
import java.util.UUID;

public final class ObservationIds {
    private static final SecureRandom RANDOM = new SecureRandom();

    private ObservationIds() {
    }

    public static String correlationId() {
        return "corr-" + UUID.randomUUID();
    }

    public static String traceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String spanId() {
        byte[] bytes = new byte[8];
        RANDOM.nextBytes(bytes);
        StringBuilder value = new StringBuilder(16);
        for (byte current : bytes) {
            value.append(String.format("%02x", current));
        }
        return value.toString();
    }
}

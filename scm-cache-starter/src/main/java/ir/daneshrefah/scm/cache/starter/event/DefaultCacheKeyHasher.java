package ir.daneshrefah.scm.cache.starter.event;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

public class DefaultCacheKeyHasher implements CacheKeyHasher {

    @Override
    public String hash(Object key) {
        if (key == null) {
            return null;
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(normalize(key).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            return null;
        }
    }

    @Override
    public String type(Object key) {
        return key == null ? null : key.getClass().getSimpleName();
    }

    private String normalize(Object key) {
        if (key instanceof String
                || key instanceof Number
                || key instanceof UUID
                || key instanceof Enum<?>) {
            return String.valueOf(key);
        }
        return key.getClass().getName() + ":" + System.identityHashCode(key);
    }
}

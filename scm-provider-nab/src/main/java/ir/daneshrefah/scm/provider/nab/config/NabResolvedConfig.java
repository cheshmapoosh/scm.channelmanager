package ir.daneshrefah.scm.provider.nab.config;

import java.util.List;
import java.util.Map;
import ir.daneshrefah.scm.provider.nab.domain.NabFieldSpec;

public record NabResolvedConfig(
        String provider,
        List<String> endpoints,
        int connectTimeoutMs,
        int socketTimeoutMs,
        int responseTimeoutMs,
        int responseIdleTimeoutMs,
        int ackLengthBytes,
        String charset,
        String userId,
        String password,
        String defaultServiceCode,
        Map<String, String> serviceCodesByTerminalType,
        Map<String, String> serviceCodesByChannelCode,
        Map<String, List<NabFieldSpec>> headerFieldsByProtocol,
        RqUid rqUid,
        CharacterNormalization characterNormalization,
        ConnectionPool connectionPool,
        boolean wireLogEnabled
) {
    public record RqUid(
            int length,
            String type
    ) {
    }

    public record CharacterNormalization(
            boolean enabled,
            Map<String, String> replacements
    ) {
    }

    public record ConnectionPool(
            boolean enabled,
            int maxTotal,
            int minIdle,
            int maxIdle,
            int maxWaitMs,
            long minEvictableIdleTimeMs,
            long softMinEvictableIdleTimeMs,
            long timeBetweenEvictionRunsMs,
            long maxLifeTimeMs,
            boolean testOnBorrow,
            boolean testOnReturn,
            boolean testWhileIdle,
            boolean blockWhenExhausted,
            boolean lifo,
            boolean prefill
    ) {
    }
}

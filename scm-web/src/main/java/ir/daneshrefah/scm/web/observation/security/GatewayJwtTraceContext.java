package ir.daneshrefah.scm.web.observation.security;

import java.time.Instant;
import java.util.List;

public record GatewayJwtTraceContext(
        String nickname,
        List<String> scopes,
        String issuer,
        String clientAddress,
        Instant issuedAt,
        Instant expiresAt,
        String channelCode,
        List<String> audiences,
        String generator,
        String transactionMethod,
        String loginMethod
) {
    private static final GatewayJwtTraceContext EMPTY = new GatewayJwtTraceContext(
            null,
            List.of(),
            null,
            null,
            null,
            null,
            null,
            List.of(),
            null,
            null,
            null
    );

    public GatewayJwtTraceContext {
        scopes = immutableList(scopes);
        audiences = immutableList(audiences);
    }

    public static GatewayJwtTraceContext empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return nickname == null
                && scopes.isEmpty()
                && issuer == null
                && clientAddress == null
                && issuedAt == null
                && expiresAt == null
                && channelCode == null
                && audiences.isEmpty()
                && generator == null
                && transactionMethod == null
                && loginMethod == null;
    }

    private static List<String> immutableList(List<String> values) {
        return values == null || values.isEmpty() ? List.of() : List.copyOf(values);
    }
}

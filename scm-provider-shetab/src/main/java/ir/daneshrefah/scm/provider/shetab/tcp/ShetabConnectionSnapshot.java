package ir.daneshrefah.scm.provider.shetab.tcp;

import java.time.Instant;

/**
 * Immutable support/readiness view. The live connection remains {@link ChannelSession}.
 */
public record ShetabConnectionSnapshot(
        String providerCode,
        ConnectionState connectionState,
        boolean verified,
        boolean recoveryRequired,
        boolean recoveryInProgress,
        boolean connectionWorkerAlive,
        String endpoint,
        long generation,
        LeaseState leaseState,
        int connectionAttempt,
        int reconnectAttempt,
        int sameEndpointFailureCount,
        Instant lastStateChangedAt,
        Instant lastConnectedAt,
        Instant lastDisconnectedAt,
        Instant lastValidatedAt,
        Instant lastFailureAt,
        String lastFailurePhase,
        String lastFailureCode,
        String lastFailureType,
        String lastFailureMessage,
        Long nextRetryDelayMs
) {
    public enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        RECONNECTING,
        FAILED,
        STOPPED
    }

    public enum LeaseState {
        NOT_REQUIRED,
        VALID,
        LOST,
        UNKNOWN
    }

    static ShetabConnectionSnapshot initial(String providerCode, boolean leasingRequired) {
        Instant now = Instant.now();
        return new ShetabConnectionSnapshot(
                providerCode,
                ConnectionState.DISCONNECTED,
                false,
                true,
                false,
                false,
                null,
                0L,
                leasingRequired ? LeaseState.UNKNOWN : LeaseState.NOT_REQUIRED,
                0,
                0,
                0,
                now,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    boolean ready() {
        return connectionState == ConnectionState.CONNECTED
                && connectionWorkerAlive
                && !recoveryRequired
                && !recoveryInProgress
                && (leaseState == LeaseState.VALID || leaseState == LeaseState.NOT_REQUIRED);
    }

    static Builder builder(ShetabConnectionSnapshot source) {
        return new Builder(source);
    }

    static final class Builder {
        String providerCode;
        ConnectionState connectionState;
        boolean verified;
        boolean recoveryRequired;
        boolean recoveryInProgress;
        boolean connectionWorkerAlive;
        String endpoint;
        long generation;
        LeaseState leaseState;
        int connectionAttempt;
        int reconnectAttempt;
        int sameEndpointFailureCount;
        Instant lastStateChangedAt;
        Instant lastConnectedAt;
        Instant lastDisconnectedAt;
        Instant lastValidatedAt;
        Instant lastFailureAt;
        String lastFailurePhase;
        String lastFailureCode;
        String lastFailureType;
        String lastFailureMessage;
        Long nextRetryDelayMs;

        private Builder(ShetabConnectionSnapshot source) {
            providerCode = source.providerCode;
            connectionState = source.connectionState;
            verified = source.verified;
            recoveryRequired = source.recoveryRequired;
            recoveryInProgress = source.recoveryInProgress;
            connectionWorkerAlive = source.connectionWorkerAlive;
            endpoint = source.endpoint;
            generation = source.generation;
            leaseState = source.leaseState;
            connectionAttempt = source.connectionAttempt;
            reconnectAttempt = source.reconnectAttempt;
            sameEndpointFailureCount = source.sameEndpointFailureCount;
            lastStateChangedAt = source.lastStateChangedAt;
            lastConnectedAt = source.lastConnectedAt;
            lastDisconnectedAt = source.lastDisconnectedAt;
            lastValidatedAt = source.lastValidatedAt;
            lastFailureAt = source.lastFailureAt;
            lastFailurePhase = source.lastFailurePhase;
            lastFailureCode = source.lastFailureCode;
            lastFailureType = source.lastFailureType;
            lastFailureMessage = source.lastFailureMessage;
            nextRetryDelayMs = source.nextRetryDelayMs;
        }

        ShetabConnectionSnapshot build() {
            return new ShetabConnectionSnapshot(
                    providerCode,
                    connectionState,
                    verified,
                    recoveryRequired,
                    recoveryInProgress,
                    connectionWorkerAlive,
                    endpoint,
                    generation,
                    leaseState,
                    connectionAttempt,
                    reconnectAttempt,
                    sameEndpointFailureCount,
                    lastStateChangedAt,
                    lastConnectedAt,
                    lastDisconnectedAt,
                    lastValidatedAt,
                    lastFailureAt,
                    lastFailurePhase,
                    lastFailureCode,
                    lastFailureType,
                    lastFailureMessage,
                    nextRetryDelayMs
            );
        }
    }
}

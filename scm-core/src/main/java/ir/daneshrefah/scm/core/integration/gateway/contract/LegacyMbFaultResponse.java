package ir.daneshrefah.scm.core.integration.gateway.contract;

/**
 * Error response shape consumed by the legacy mobile-banking UI.
 */
public record LegacyMbFaultResponse(
        String detail,
        int code,
        String messageKey,
        String text,
        String httpCode) {
}

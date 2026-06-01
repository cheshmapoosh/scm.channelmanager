package ir.daneshrefah.scm.core.integration.gateway.contract;

public record ClientContract(
        String name,
        String requestDecoder,
        String responseEncoder,
        String faultEncoder) {
}

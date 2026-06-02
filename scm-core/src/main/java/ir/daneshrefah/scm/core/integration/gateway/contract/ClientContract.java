package ir.daneshrefah.scm.core.integration.gateway.contract;

public record ClientContract(
        String name,
        String requestDecoder,
        String responseEncoder,
        String faultEncoder,
        String version) {

    public ClientContract(String name,
                          String requestDecoder,
                          String responseEncoder,
                          String faultEncoder) {
        this(name, requestDecoder, responseEncoder, faultEncoder, null);
    }
}

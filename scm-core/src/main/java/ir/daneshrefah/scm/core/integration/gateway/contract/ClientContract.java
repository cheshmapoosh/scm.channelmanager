package ir.daneshrefah.scm.core.integration.gateway.contract;

public record ClientContract(
        String name,
        String requestDecoder,
        String responseEncoder,
        String faultEncoder,
        String version,
        String source) {

    public ClientContract(String name,
                          String requestDecoder,
                          String responseEncoder,
                          String faultEncoder) {
        this(name, requestDecoder, responseEncoder, faultEncoder, null, null);
    }

    public ClientContract(String name,
                          String requestDecoder,
                          String responseEncoder,
                          String faultEncoder,
                          String version) {
        this(name, requestDecoder, responseEncoder, faultEncoder, version, null);
    }
}

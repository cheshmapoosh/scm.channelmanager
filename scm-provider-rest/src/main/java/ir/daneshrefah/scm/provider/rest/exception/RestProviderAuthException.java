package ir.daneshrefah.scm.provider.rest.exception;

public class RestProviderAuthException extends RuntimeException {
    private final RestProviderAuthFault fault;
    private final String providerCode;

    public RestProviderAuthException(RestProviderAuthFault fault, String providerCode, String message) {
        super(message);
        this.fault = fault;
        this.providerCode = providerCode;
    }

    public RestProviderAuthException(RestProviderAuthFault fault, String providerCode, String message, Throwable cause) {
        super(message, cause);
        this.fault = fault;
        this.providerCode = providerCode;
    }

    public RestProviderAuthFault fault() {
        return fault;
    }

    public String providerCode() {
        return providerCode;
    }
}

package ir.daneshrefah.scm.observation.starter.gateway;

public enum GatewayProtocol {
    HTTP,
    SOAP,
    MQ,
    JMS,
    TCP,
    ISO8583,
    UNKNOWN;

    public String value() {
        return name().toLowerCase();
    }
}

package ir.daneshrefah.scm.provider.nab.domain;

public record NabWireRequest(
        String protocol,
        String fullMessage
) {
    public String bodyWithoutProtocol() {
        if (fullMessage == null || fullMessage.length() <= protocol.length()) {
            return "";
        }
        return fullMessage.substring(protocol.length());
    }
}

package ir.daneshrefah.scm.provider.nab.domain;

public record NabCommandSpec(
        String code,
        NabProtocol protocol
) {
}

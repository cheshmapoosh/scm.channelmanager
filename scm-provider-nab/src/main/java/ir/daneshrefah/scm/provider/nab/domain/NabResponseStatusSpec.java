package ir.daneshrefah.scm.provider.nab.domain;

public record NabResponseStatusSpec(
        NabFieldSpec field,
        String successCode,
        String successListCode
) {
}

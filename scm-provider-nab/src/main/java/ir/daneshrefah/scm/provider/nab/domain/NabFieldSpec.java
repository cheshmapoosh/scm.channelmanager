package ir.daneshrefah.scm.provider.nab.domain;

public record NabFieldSpec(
        String name,
        String path,
        int length,
        NabFieldType type,
        boolean required,
        String converter,
        NabPadding padding,
        NabOverflowPolicy overflow,
        boolean trim
) {
}

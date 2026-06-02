package ir.daneshrefah.scm.provider.nab.domain;

import java.util.List;

public record NabResponseSpec(
        NabResponseStatusSpec status,
        String recordSeparator,
        List<NabFieldSpec> fields
) {
}

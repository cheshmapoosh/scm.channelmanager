package ir.daneshrefah.scm.common.model.audit.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum RevisionType {
    INSERT(0), UPDATE(1), DELETE(2);
    private final int status;

    public static Optional<RevisionType> findByStatus(int status) {
        return Arrays.stream(values())
                .filter(revisionType -> revisionType.getStatus() == status)
                .findFirst();
    }
}

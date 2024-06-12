package ir.daneshrefah.scm.process.model.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.stream.Stream;

/**
 * @author m.farahani
 * @version 1.0
 * @since 2024-01-28
 */

@Getter
@RequiredArgsConstructor
public enum UserTaskStatus implements Serializable {
    WAITING("WAITING"),//TOOD add persian status name
    EXPIRED("EXPIRED"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    REVOKED("REVOKED"),
    FAILED("FAILED");

    private final String key;

    public static UserTaskStatus fromString(String key) {
        return Stream.of(UserTaskStatus.values())
                .filter(e -> e.getKey().equals(key))
                .findFirst()
                .orElse(null);
    }
}

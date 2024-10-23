package ir.daneshrefah.scm.common.model.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ServiceProviderStatus {

    ACTIVE(1),
    DE_ACTIVE(2);

    private final Integer value;

    public static ServiceProviderStatus fromValue(Integer value) {
        return Arrays.stream(values())
                .filter(v -> v.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }
}

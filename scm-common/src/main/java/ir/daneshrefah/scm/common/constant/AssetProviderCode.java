package ir.daneshrefah.scm.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AssetProviderCode {
    NAB("NAB"), CCP("CCP");
    private final String value;

    public static AssetProviderCode find(String value) {
        return Arrays.stream(values())
                .filter(assetProviderCode -> assetProviderCode.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }
}

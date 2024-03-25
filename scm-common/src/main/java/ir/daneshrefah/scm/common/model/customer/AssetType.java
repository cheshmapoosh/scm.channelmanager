package ir.daneshrefah.scm.common.model.customer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
@Getter
@RequiredArgsConstructor
public enum AssetType {

    ACCOUNT(1), LOAN(2), CARD(3);

    private final Integer code;

    public static AssetType findByCode(Integer code) {
        for (AssetType enumValue : AssetType.values()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        return null;
    }

}

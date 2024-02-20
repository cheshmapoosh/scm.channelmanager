package ir.daneshrefah.scm.common.model.notification.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
@RequiredArgsConstructor
@Getter
public enum TemplateCode {

    AUTHENTICATION("authentication"),
    ACTIVATION("activation"),
    GENERAL("general");

    private final String value;

    public static TemplateCode findByCode(String value) {
        return Arrays.stream(values())
                .filter(templateCode -> templateCode.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }

}

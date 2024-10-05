package ir.daneshrefah.scm.common.model.service;

import java.util.Arrays;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-17
 */
public enum ExternalServiceBodyType {

    NONE, MESSAGE_BODY, PARAMETERS;

    public static ExternalServiceBodyType find(String name){
        if (Objects.nonNull(name)){
           return Arrays.stream(values())
                    .filter(type-> type.name().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}

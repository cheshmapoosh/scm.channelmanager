package ir.daneshrefah.scm.common.model.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-06
 */
@AllArgsConstructor
public enum ErrorType {

    VALIDATION("SCM-1001"), ACCESS_DENIED("SCM-1002"), AUTHENTICATION_FAILED("SCM-1003");

    @Getter
    private String code;

}

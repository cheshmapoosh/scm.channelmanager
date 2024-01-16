package ir.daneshrefah.scm.common.model.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-06
 */
@AllArgsConstructor
public enum ErrorType {

    VALIDATION(ERROR_CODE_VALIDATION), ACCESS_DENIED(ERROR_CODE_ACCESS_DENIED), AUTHENTICATION_FAILED(ERROR_CODE_AUTHENTICATION_FAILED),
    HOST_UNREACHABLE(ERROR_CODE_HOST_UNREACHABLE), INVALID_PROVIDER_RESPONSE(ERROR_CODE_INVALID_PROVIDER_RESPONSE);

    @Getter
    private String code;

}
